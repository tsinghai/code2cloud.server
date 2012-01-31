/*******************************************************************************
 * Copyright (c) 2010, 2012 Tasktop Technologies
 * Copyright (c) 2010, 2011 SpringSource, a division of VMware
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 ******************************************************************************/
package com.tasktop.c2c.server.services.web.scm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.web.HttpRequestHandler;

import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.io.FlushingChunkedOutputStream;
import com.tasktop.c2c.server.common.service.io.InputPipe;
import com.tasktop.c2c.server.common.service.io.MultiplexingOutputStream;
import com.tasktop.c2c.server.common.service.io.PacketType;

/**
 * A handler for Git requests initiated via SSH at the ALM hub.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class GitHandler implements HttpRequestHandler {

	private enum GitCommand {
		RECEIVE_PACK("git-receive-pack", "receive-pack", Role.User), UPLOAD_PACK("git-upload-pack", "upload-pack",
				Role.Observer, Role.Community, Role.User);

		private final String commandName;
		private final String[] roles;
		private final String commandLine;

		private GitCommand(String commandName, String commandLine, String... roles) {
			this.commandName = commandName;
			this.commandLine = commandLine;
			this.roles = roles;
		}

		public String getCommandLine() {
			return commandLine;
		}

		public static final GitCommand fromCommandName(String commandName) {
			for (GitCommand command : values()) {
				if (command.getCommandName().equals(commandName)) {
					return command;
				}
			}
			return null;
		}

		public String getCommandName() {
			return commandName;
		}

		public String[] getRoles() {
			return roles;
		}
	}

	private static final String MIME_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final Pattern GIT_COMMAND_PATTERN = Pattern.compile("/(git-upload-pack|git-receive-pack)/(.*)");

	private static Boolean chunkedIOContainerSupported;

	private Logger log = LoggerFactory.getLogger(GitHandler.class.getName());

	private String gitRoot;

	private int bufferSize = 1024 * 16;
	private File workingDirectory;
	private String uploadPackCommand;
	private String receivePackCommand;
	private HashMap<String, String> environment = new HashMap<String, String>();
	private long timeoutInMillis = 1000L * 60L * 60L * 2L;

	@SuppressWarnings("serial")
	private static class ErrorResponseException extends Exception {
		private ErrorResponseException(String message) {
			super(message);
		}
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		final boolean containerSupportsChunkedIO = computeContainerSupportsChunkedIO();

		String pathInfo = request.getPathInfo();
		log.info("Git request: " + request.getMethod() + " " + request.getRequestURI() + " " + pathInfo);
		try {
			// only work on Git requests
			Matcher matcher = pathInfo == null ? null : GIT_COMMAND_PATTERN.matcher(pathInfo);
			if (matcher == null || !matcher.matches()) {
				log.info("Unexpected path: " + pathInfo);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			String requestCommand = matcher.group(1);
			String requestPath = matcher.group(2);
			String gitCommand = null;
			String[] args = null;
			// sanity check on path, disallow path separator components
			if (requestPath == null || requestPath.contains("/") || requestPath.contains("..")) {
				badPathResponse();
			}

			// identify the git command
			GitCommand command = GitCommand.fromCommandName(requestCommand);
			if (command != null) {
				// permissions check
				if (!Security.hasOneOfRoles(command.getRoles())) {
					log.info("Access denied to " + Security.getCurrentUser() + " for " + command.getCommandName()
							+ " on " + TenancyContextHolder.getContext().getTenant() + " " + requestPath);
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
				switch (command) {
				case RECEIVE_PACK:
					gitCommand = receivePackCommand;
					break;
				case UPLOAD_PACK:
					gitCommand = uploadPackCommand;
					break;
				default:
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				if (gitCommand == null) {
					log.error("Git command " + requestCommand + " is not correctly configured");
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}
				args = new String[] { command.getCommandLine(), computeGitPath(requestPath) };
			}
			if (gitCommand == null) {
				badPathResponse();
			}

			// we spawn a Git process to handle the request
			Process process = null;
			try {
				process = createProcess(gitCommand, args);
			} catch (IOException e) {
				log.error("Cannot create Git process " + gitCommand + ": " + e.getMessage(), e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			MultiplexingOutputStream mox = null;

			long startTime = System.currentTimeMillis();
			try {
				// indicate that we're ok to handle the request
				// note that following this there will be a two-way communication with the process
				// that might still encounter errors. That's ok.
				startOkResponse(response, containerSupportsChunkedIO);

				List<InputPipe> tasks = new ArrayList<InputPipe>(3);
				// pipe stderr, stdout
				mox = createMultiplexingOutputStream(response, containerSupportsChunkedIO);

				tasks.add(new InputPipe(process.getErrorStream(), mox.stream(PacketType.STDERR), bufferSize, null)
						.flush(true).description("stderr"));
				tasks.add(new InputPipe(process.getInputStream(), mox.stream(PacketType.STDOUT), bufferSize, null)
						.flush(true).description("stdout"));

				List<Callable<Void>> processCallables = new ArrayList<Callable<Void>>(tasks);

				// pipe request input to the process input
				InputStream requestInput = request.getInputStream();
				if (!containerSupportsChunkedIO) {
					requestInput = new ChunkedInputStream(requestInput);
				}
				tasks.add(new InputPipe(requestInput, process.getOutputStream(), bufferSize, Thread.currentThread())
						.flush(true).description("request input"));

				// start these pipes
				ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
				try {
					List<Future<Void>> processFutures = new ArrayList<Future<Void>>();
					for (Callable<Void> task : tasks) {
						Future<Void> future = executor.submit(task);
						if (processCallables.contains(task)) {
							processFutures.add(future);
						}
					}
					for (Future<Void> processFuture : processFutures) {
						// wait until completed, but implement a timeout so that we don't wait forever
						long elapsed = System.currentTimeMillis() - startTime;
						long remainingBeforeTimeout = timeoutInMillis - elapsed;
						if (remainingBeforeTimeout < 0) {
							remainingBeforeTimeout = 10L;
						}
						processFuture.get(remainingBeforeTimeout, TimeUnit.MILLISECONDS);
					}

					// gracefully cancel pipes.
					for (InputPipe pipe : tasks) {
						pipe.cancel();
					}

					// if we reach here both the stdin and stderr of the process are likely closed,
					// so we expect the process to be terminated. The request stream may remain open
					// indefinitely, so we're done.

					// at this stage we're done with IO
					// send the exit value and closing chunk
					try {
						int exitValue;
						for (;;) {
							try {
								exitValue = process.waitFor();
								break;
							} catch (InterruptedException e) {
								log.debug("Waiting for process interrupted", e);
								Thread.interrupted();
							}
						}
						if (exitValue != 0) {
							log.info("Exit value: " + exitValue);
						}
						mox.writeExitCode(exitValue);
						mox.close();
					} catch (IOException e) {
						// ignore
						log.debug("Cannot complete writing exit state", e);
					}

				} catch (TimeoutException e) {
					long elapsed = System.currentTimeMillis() - startTime;
					log.error("Git command " + gitCommand + " timed out after " + elapsed + " milliseconds");
				} catch (InterruptedException e) {
					Thread.interrupted();
					// we likely lost our request input/output connection due to user abort
					log.error("Interruped", e);
				} catch (ExecutionException e) {
					log.error("Unexpected exception " + e.getMessage(), e);
				} finally {
					if (!executor.isTerminated()) {
						Thread.interrupted();
						executor.shutdownNow();
						try {
							executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							// ignore
						}
					}
					// clear interrupt status
					Thread.interrupted();
				}
			} finally {
				process.destroy();
			}
		} catch (ErrorResponseException e) {
			createGitErrorResponse(response, containerSupportsChunkedIO, e.getMessage());
		} finally {
			log.info("Git request complete");
		}
	}

	private void badPathResponse() throws ErrorResponseException {
		throw new ErrorResponseException("Path does not appear to be a git repository");
	}

	private void createGitErrorResponse(HttpServletResponse response, boolean containerSupportsChunkedIO, String message)
			throws IOException {
		startOkResponse(response, containerSupportsChunkedIO);
		MultiplexingOutputStream mox = createMultiplexingOutputStream(response, containerSupportsChunkedIO);

		OutputStream errorStream = mox.stream(PacketType.STDERR);
		errorStream.write(message.getBytes());
		if (!message.endsWith("\n")) {
			errorStream.write('\n');
		}
		errorStream.flush();

		mox.writeExitCode(1);
		mox.close();
	}

	private MultiplexingOutputStream createMultiplexingOutputStream(HttpServletResponse response,
			final boolean containerSupportsChunkedIO) throws IOException {
		MultiplexingOutputStream mox;
		OutputStream outputStream = response.getOutputStream();

		if (!containerSupportsChunkedIO) {
			outputStream = new FlushingChunkedOutputStream(outputStream);
		}
		mox = new MultiplexingOutputStream(outputStream);
		return mox;
	}

	private void startOkResponse(HttpServletResponse response, final boolean containerSupportsChunkedIO) {
		response.setStatus(HttpServletResponse.SC_OK);
		addNoCacheHeaders(response);
		response.setHeader("Content-Type", MIME_TYPE_APPLICATION_OCTET_STREAM);
		if (!containerSupportsChunkedIO) {
			response.setHeader("Transfer-Encoding", "chunked");
		}
	}

	private static boolean computeContainerSupportsChunkedIO() {
		synchronized (GitHandler.class) {
			if (chunkedIOContainerSupported == null) {
				boolean supported = true;
				for (StackTraceElement stackTrace : Thread.currentThread().getStackTrace()) {
					if (stackTrace.getClassName().contains("winstone")) {
						supported = false;
						break;
					}
				}
				chunkedIOContainerSupported = supported;
			}
		}
		return chunkedIOContainerSupported;
	}

	/**
	 * compute the path to the git repository
	 */
	private String computeGitPath(String requestPath) {
		String path = gitRoot + "/" + TenancyContextHolder.getContext().getTenant().getIdentity() + "/"
				+ GitConstants.HOSTED_GIT_DIR;
		if (!path.endsWith("/") && !requestPath.startsWith("/")) {
			path += "/";
		}
		path += requestPath;
		return path;
	}

	protected Process createProcess(String command, String... args) throws IOException {
		String[] processCommand = new String[args == null ? 1 : args.length + 1];
		processCommand[0] = command;
		if (args != null && args.length > 0) {
			System.arraycopy(args, 0, processCommand, 1, args.length);
		}

		@SuppressWarnings("unchecked")
		Map<String, String> processEnvironment = (Map<String, String>) environment.clone();
		configureProcessEnvironment(processEnvironment);

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(processCommand);
		processBuilder.directory(workingDirectory);
		processBuilder.environment().putAll(processEnvironment);
		return processBuilder.start();
	}

	/**
	 * @param processEnvironment
	 */
	private void configureProcessEnvironment(Map<String, String> processEnvironment) {
		String currentUser = Security.getCurrentUser();
		if (currentUser != null) {
			processEnvironment.put("GIT_COMMITTER_NAME", currentUser);
			// see http-backend.c, we may want to do something like user@http.host for email
			processEnvironment.put("GIT_COMMITTER_EMAIL", currentUser);
		}
	}

	private void addNoCacheHeaders(HttpServletResponse response) {
		// expire in the past
		response.addHeader("Expires", "Fri, 01 Jan 2000 00:00:00 GMT");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
	}

	public String getGitRoot() {
		return gitRoot;
	}

	public void setGitRoot(String gitRoot) {
		this.gitRoot = gitRoot;
	}

	public String getUploadPackCommand() {
		return uploadPackCommand;
	}

	public void setUploadPackCommand(String uploadPackCommand) {
		this.uploadPackCommand = uploadPackCommand;
	}

	public String getReceivePackCommand() {
		return receivePackCommand;
	}

	public void setReceivePackCommand(String receivePackCommand) {
		this.receivePackCommand = receivePackCommand;
	}

	public HashMap<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(HashMap<String, String> environment) {
		this.environment = environment;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public long getTimeoutInMillis() {
		return timeoutInMillis;
	}

	public void setTimeoutInMillis(long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}

}
