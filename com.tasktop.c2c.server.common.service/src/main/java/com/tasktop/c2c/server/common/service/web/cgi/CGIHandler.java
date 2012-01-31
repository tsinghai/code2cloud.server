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
package com.tasktop.c2c.server.common.service.web.cgi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tasktop.c2c.server.common.service.Security;

/**
 * a handler for executing requests based on the Common Gateway Interface (CGI) specification found here:
 * http://www.rfc-editor.org/rfc/rfc3875.txt
 * 
 * Cloneable so that it can be used with the template design pattern.
 * 
 * @author David Green
 */
public class CGIHandler implements Cloneable {
	private static Logger logger = LoggerFactory.getLogger(CGIHandler.class);

	private static final class CGIResponseHandler implements Callable<Object> {
		private final HttpServletResponse response;
		private InputStream processOutput;

		private CGIResponseHandler(HttpServletResponse response, Process process) {
			this.response = response;
			processOutput = new BufferedInputStream(process.getInputStream());
		}

		@Override
		public Object call() throws Exception {
			// CGI response RFC 3875 section 6
			try {
				CGIMessageHeaderReader headerReader = new CGIMessageHeaderReader(processOutput);
				headerReader.readHeader();

				response.setStatus(headerReader.getHttpStatusCode());
				for (Entry<String, String> header : headerReader.getHeaders().entrySet()) {
					response.setHeader(header.getKey(), header.getValue());
				}
				OutputStream responseOut = response.getOutputStream();
				int bytes;
				byte[] buffer = new byte[1024 * 8];
				while ((bytes = processOutput.read(buffer)) != -1) {
					responseOut.write(buffer, 0, bytes);
				}
				// We dont' really need to close the output stream here, let the servlet container handle it instead.
				// This works around task 993
			} finally {
				processOutput.close();
			}
			return null;
		}
	}

	private static class CGIInputFlow implements Callable<Object> {
		private final InputStream input;
		private OutputStream cgiInput;
		private final int expectedByteCount;

		private CGIInputFlow(Process process, InputStream input, int byteCount) {
			this.expectedByteCount = byteCount;
			cgiInput = process.getOutputStream();
			this.input = input;
		}

		@Override
		public Object call() throws Exception {
			int bytes;
			byte[] buffer = new byte[1024 * 8];

			int count = 0;
			try {
				while ((bytes = input.read(buffer)) != -1) {
					cgiInput.write(buffer, 0, bytes);
					count = count + bytes;
				}
			} finally {
				cgiInput.close();
			}

			if (expectedByteCount != count) {
				logger.info("Expected to read [" + expectedByteCount + "] bytes, but really read [" + count + "] bytes");
			}

			return null;
		}
	}

	private final class ErrorStreamLogger implements Callable<Object> {
		private BufferedReader reader;

		private ErrorStreamLogger(Process process) {
			reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		}

		@Override
		public Object call() throws Exception {
			String line;
			while ((line = reader.readLine()) != null) {
				logger.error(line);
			}
			return null;
		}
	}

	private static final String SERVER_SOFTWARE = CGIHandler.class.getName() + "/1.0";
	private static final Map<String, String> requestHeaderToEnvironmentName = new HashMap<String, String>();
	static {
		registerRequestHeaderToEnvironmentName("Content-Length", "CONTENT_LENGTH");
		registerRequestHeaderToEnvironmentName("Content-Type", "CONTENT_TYPE");
	}

	private String command;
	private Map<String, String> environment = new HashMap<String, String>();

	private String pathInfo;
	private String pathTranslated;

	private File workingDirectory;

	private long timeoutInMillis = 1000L * 60L * 60L;

	public void service(HttpServletRequest request, final HttpServletResponse response) throws IOException {
		if (command == null) {
			throw new IllegalStateException();
		}
		configure(request);

		configureEnvironment(request);

		logger.info(pathInfo + " as " + environment);

		boolean responseComplete = false;
		boolean timeout = false;
		try {
			// section 4.4 re: indexed queries and command-line parameters: not implemented
			final int requestContentLength = request.getContentLength();

			long startTime = System.currentTimeMillis();
			final Process process = createProcess();
			try {
				List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(3);
				// ORDER DEPENDENCY: this task comes first
				tasks.add(new CGIResponseHandler(response, process));
				tasks.add(new ErrorStreamLogger(process));
				if (needsRequestInput(request)) {
					tasks.add(new CGIInputFlow(process, request.getInputStream(), requestContentLength));
				}
				ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
				try {
					for (Callable<Object> task : tasks) {
						executor.submit(task);
					}
					long elapsed = System.currentTimeMillis() - startTime;
					long remainingBeforeTimeout = timeoutInMillis - elapsed;
					executor.shutdown();
					if (!executor.awaitTermination(remainingBeforeTimeout, TimeUnit.MILLISECONDS)) {
						timeout = true;
						responseComplete = false;
					} else {
						responseComplete = true;
					}

				} catch (InterruptedException e) {
					throw new IllegalStateException(e); // Should not happen.
				}
			} finally {
				process.destroy();
			}

		} finally {
			if (!responseComplete) {
				try {
					response.sendError(timeout ? HttpServletResponse.SC_REQUEST_TIMEOUT
							: HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	// meta-variable-name = "AUTH_TYPE" | "CONTENT_LENGTH" |
	// "CONTENT_TYPE" | "GATEWAY_INTERFACE" |
	// "PATH_INFO" | "PATH_TRANSLATED" |
	// "QUERY_STRING" | "REMOTE_ADDR" |
	// "REMOTE_HOST" | "REMOTE_IDENT" |
	// "REMOTE_USER" | "REQUEST_METHOD" |
	// "SCRIPT_NAME" | "SERVER_NAME" |
	// "SERVER_PORT" | "SERVER_PROTOCOL" |
	// "SERVER_SOFTWARE" | scheme |
	// protocol-var-name | extension-var-name
	// protocol-var-name = ( protocol | scheme ) "_" var-name
	// scheme = alpha *( alpha | digit | "+" | "-" | "." )
	// var-name = token
	// extension-var-name = token
	protected void configureEnvironment(HttpServletRequest request) {
		String currentUser = Security.getCurrentUser();
		if (currentUser != null) {
			environment.put("AUTH_TYPE", "almauth");
			environment.put("REMOTE_USER", currentUser);
		}
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			String headerName = headerNames.nextElement();
			String envName = getEnvironmentVariableNameOfRequestHeader(headerName);
			if (envName != null) {
				environment.put(envName, nullToBlank(request.getHeader(headerName)));
			} else {
				if ("Authorization".equalsIgnoreCase(headerName) || "Connection".equalsIgnoreCase(headerName)
						|| "Proxy-Authorization".equalsIgnoreCase(headerName)) {
					continue;
				}
				// section 4.1.18
				environment.put("HTTP_" + headerName.replace('-', '_').toUpperCase(),
						nullToBlank(request.getHeader(headerName)));
			}
		}
		environment.put("GATEWAY_INTERFACE", "CGI/1.1");
		environment.put("PATH_INFO", nullToBlank(pathInfo));
		environment.put("PATH_TRANSLATED", nullToBlank(pathTranslated));
		environment.put("QUERY_STRING", nullToBlank(request.getQueryString()));
		environment.put("REMOTE_ADDR", nullToBlank(request.getRemoteAddr()));
		environment.put("REMOTE_HOST", nullToBlank(request.getRemoteAddr())); // don't do remote host on purpose: no DNS
		environment.put("REQUEST_METHOD", request.getMethod());
		environment.put("SCRIPT_NAME", command);
		environment.put("SCRIPT_FILENAME", command); // PHP likes this
		environment.put("SERVER_NAME", nullToBlank(request.getServerName()));
		environment.put("SERVER_PORT", String.valueOf(request.getServerPort()));
		environment.put("SERVER_PROTOCOL", String.valueOf(request.getProtocol()));
		environment.put("SERVER_SOFTWARE", SERVER_SOFTWARE);
	}

	private boolean needsRequestInput(HttpServletRequest request) throws IOException {
		// Special handling for GET method: we don't expect content there, and trying to read from the input in such
		// cases can provoke ajp issues.
		// See task 944, 718
		return (!request.getMethod().equals("GET")) && request.getContentLength() != 0
				&& request.getInputStream() != null;
	}

	protected void handleException(ExecutionException e) {
		logger.error("Unexpected exception invoking " + command, e.getCause());
	}

	protected Process createProcess() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(new String[] { command });
		processBuilder.directory(workingDirectory);
		processBuilder.environment().putAll(environment);
		return processBuilder.start();
	}

	protected void configure(HttpServletRequest request) {
		if (pathInfo == null) {
			pathInfo = request.getPathInfo();
		}
		if (pathTranslated == null) {
			pathTranslated = request.getPathTranslated();
		}
		if (workingDirectory == null) {
			String pathTranslated = request.getPathTranslated();
			if (pathTranslated != null) {
				File file = new File(pathTranslated);
				while (file != null && !file.exists() || !file.isDirectory()) {
					file = file.getParentFile();
				}
				workingDirectory = file;
			}
			if (workingDirectory == null) {
				throw new IllegalStateException("could not compute working directory for [" + request.getRequestURI()
						+ "]. This can happen when tomcat has run out of space to extract the webapp.");
			}
		}
	}

	private String nullToBlank(String s) {
		return s == null ? "" : s;
	}

	private String getEnvironmentVariableNameOfRequestHeader(String headerName) {
		return requestHeaderToEnvironmentName.get(headerName.toLowerCase());
	}

	private static void registerRequestHeaderToEnvironmentName(String requestHeader, String envName) {
		requestHeaderToEnvironmentName.put(requestHeader.toLowerCase(), envName);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public String getPathTranslated() {
		return pathTranslated;
	}

	public void setPathTranslated(String pathTranslated) {
		this.pathTranslated = pathTranslated;
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

	public Logger getLogger() {
		return logger;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	@Override
	public CGIHandler clone() {
		try {
			CGIHandler copy = (CGIHandler) super.clone();
			copy.environment = new HashMap<String, String>();
			copy.environment.putAll(environment);
			copy.workingDirectory = workingDirectory;
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

}
