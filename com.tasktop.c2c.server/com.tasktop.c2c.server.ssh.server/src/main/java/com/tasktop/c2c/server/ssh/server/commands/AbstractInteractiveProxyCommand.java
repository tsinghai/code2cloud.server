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
package com.tasktop.c2c.server.ssh.server.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.auth.service.proxy.AuthenticationTokenSerializer;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.io.FlushingChunkedOutputStream;
import com.tasktop.c2c.server.common.service.io.InputPipe;
import com.tasktop.c2c.server.common.service.io.MultiplexingInputStream;
import com.tasktop.c2c.server.common.service.io.PacketType;
import com.tasktop.c2c.server.common.service.web.HeaderConstants;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;
import com.tasktop.c2c.server.ssh.server.Constants;

/**
 * An implementation of "interactive" proxy commands, which use HTTP to proxy to an ALM service node, using chunked
 * transfer-encoding to enable a two-way conversation between the client and the command implementation.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public abstract class AbstractInteractiveProxyCommand extends AbstractCommand {

	private static final String MIME_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final Pattern PATH_PATTERN = Pattern.compile("/([^/]+)/(.+)");

	private static final String HTTP_ENTITY_CHARSET = "US-ASCII";

	@Autowired(required = true)
	private ProjectServiceService projectServiceService;

	@Autowired(required = true)
	@Qualifier("sshCommandExecutorService")
	private ExecutorService executorService;

	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	@Autowired
	private ProxySocketFactory socketFactory;

	private AuthenticationTokenSerializer tokenSerializer = new AuthenticationTokenSerializer();

	private int bufferSize = 1024 * 16;

	protected Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Override
	public void setExitCallback(final ExitCallback callback) {
		// sshd exit callback doesn't pass the exit message to the client
		// so we do that here.
		ExitCallback callbackWrapper = new ExitCallback() {

			@Override
			public void onExit(int exitValue) {
				callback.onExit(exitValue);
			}

			@Override
			public void onExit(int exitValue, String exitMessage) {
				if (exitMessage != null) {
					try {
						err.write(exitMessage.getBytes());// encoding to use?
						err.write('\n');
						err.flush();
					} catch (IOException e) {
						getLogger().debug("Cannot write error message: " + e.getMessage(), e);
					}
				}
				callback.onExit(exitValue);
			}

		};
		super.setExitCallback(callbackWrapper);
	}

	@Override
	public void start(final Environment env) throws IOException {
		final AuthenticationToken authenticationToken = session
				.getAttribute(Constants.SESSION_KEY_AUTHENTICATION_TOKEN);
		if (authenticationToken == null) {
			// should never happen
			throw new IllegalStateException();
		}

		try {
			String uri = computeCommandPath();

			// path info format is:
			// /<project-identity>/<repository>.git

			Matcher matcher = PATH_PATTERN.matcher(uri);
			if (matcher.matches()) {
				final String projectId = matcher.group(1);
				final String path = matcher.group(2);
				if (path == null || path.indexOf('/') != -1) {
					// short-circuit everything: currently we don't support repositories with nested paths
					pathNotFound(uri);
				}

				final String requestPath = computeRequestPath(path);

				final ProjectService service;
				try {
					service = projectServiceService.findServiceByUri(projectId, requestPath);
				} catch (EntityNotFoundException e) {
					getLogger().info("Project identity not found: " + projectId);
					pathNotFound(uri);
					throw new IllegalStateException();
				}
				if (service == null) {
					getLogger().info("No ProjectService associated with project " + projectId + " path " + requestPath);
					pathNotFound(uri);
				}
				if (!service.getServiceHost().isAvailable()
						|| service.getServiceHost().getInternalNetworkAddress() == null) {
					getLogger().info("Service temporarily unavailable for " + projectId + " path " + requestPath);
					throw new CommandException(1, "Repository temporarily unavailable.  Please try again later.");
				}
				final Boolean projectIsPublic = service.getProjectServiceProfile().getProject().getPublic();
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						// setup the security context
						AuthenticationToken projectSpecializedToken = internalAuthenticationService
								.specializeAuthenticationToken(authenticationToken, projectId, projectIsPublic);
						AuthenticationServiceUser user = AuthenticationServiceUser
								.fromAuthenticationToken(projectSpecializedToken);
						SecurityContextHolder.getContext().setAuthentication(
								new PreAuthenticatedAuthenticationToken(user, projectSpecializedToken, user
										.getAuthorities()));
						try {
							// setup the tenancy context
							TenancyContextHolder.createEmptyContext();
							TenancyContextHolder.getContext().setTenant(new DefaultTenant(projectId, null));
							try {
								if (!hasRequiredRoles()) {
									getLogger().info(
											"Access denied to " + user.getUsername() + " for " + getName()
													+ " project:" + projectId + " path:" + requestPath);
									throw new CommandException(1, "Permission denied");
								}

								// propagate the tenant and authentication via request headers
								RequestHeadersSupport headers = new RequestHeadersSupport();
								headers.addHeader(HeaderConstants.TENANT_HEADER, projectId);
								tokenSerializer.serialize(headers, projectSpecializedToken);

								performCommand(env, service, projectId, path, requestPath, headers);
								callback.onExit(0);
							} catch (CommandException e) {
								callback.onExit(e.getReturnCode(), e.getMessage());
							} catch (Throwable t) {
								callback.onExit(-1, t.getClass().getSimpleName() + ": " + t.getMessage());
								getLogger().error(t.getMessage(), t);
							} finally {
								TenancyContextHolder.clearContext();
							}
						} finally {
							SecurityContextHolder.clearContext();
						}
					}
				});
			} else {
				getLogger().info("Repository path does not match expected pattern: " + uri);
				pathNotFound(uri);
			}
		} catch (CommandException e) {
			getLogger().info("CommandException: " + e.getMessage(), e);
			callback.onExit(e.getReturnCode(), e.getMessage());
		}
	}

	private void pathNotFound(String path) throws CommandException {
		throw new CommandException(1, String.format("'%s' does not appear to be a git repositoy", path));
	}

	private final boolean hasRequiredRoles() {
		return Security.hasOneOfRoles(getRequiredRoles());
	}

	protected abstract String[] getRequiredRoles();

	protected void performCommand(Environment env, ProjectService service, String projectId, String path,
			String requestPath, RequestHeadersSupport headers) throws CommandException {
		String internalProxyUri = service.computeInternalProxyBaseUri(false);
		if (internalProxyUri == null) {
			throw new IllegalStateException();
		}
		URI targetUri;
		try {
			if (!internalProxyUri.endsWith("/")) {
				internalProxyUri += "/";
			}
			internalProxyUri += getName() + '/' + path;

			targetUri = new URI(internalProxyUri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		String host = targetUri.getHost();
		int port = targetUri.getPort();
		if (port < 0) {
			port = 80;
		}
		if (targetUri.getScheme() == null || !targetUri.getScheme().equalsIgnoreCase("http")) {
			throw new IllegalStateException("scheme " + targetUri.getScheme() + " is not supported");
		}
		HeaderGroup headerGroup = computeHeaders(targetUri);
		for (Entry<String, List<String>> headerEntry : headers.getRequestHeaders().entrySet()) {
			for (String value : headerEntry.getValue()) {
				headerGroup.addHeader(new Header(headerEntry.getKey(), value));
			}
		}
		getLogger().info("Proxying " + getName() + " to " + targetUri);
		try {
			Socket socket = socketFactory.openConnection(host, port);
			try {
				// initiate an HTTP request with Transfer-Encoding: chunked
				OutputStream proxyOut = socket.getOutputStream();
				emitHttpRequestLine(proxyOut, targetUri);
				emitHeaders(proxyOut, headerGroup);

				proxyOut.flush();

				List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(3);
				FlushingChunkedOutputStream chunkedRequestOut = new FlushingChunkedOutputStream(proxyOut);
				tasks.add(new InputPipe(in, chunkedRequestOut, bufferSize, Thread.currentThread()).flush(true));

				// start these pipes
				ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
				try {
					for (Callable<Void> task : tasks) {
						executor.submit(task);
					}

					InputStream proxyInput = socket.getInputStream();
					try {
						readHttpResponse(proxyInput);
						MultiplexingInputStream input = new MultiplexingInputStream(new ChunkedInputStream(proxyInput));
						for (;;) {
							PacketType packetType = input.getPacketType();
							if (packetType == null) {
								break;
							}
							int length = input.getPacketLength();

							processData(input, packetType, length);
						}
					} finally {
						try {
							executor.shutdown();
							executor.awaitTermination(1000L, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							// ignore
						}
					}
				} finally {
					executor.shutdownNow();
					try {
						executor.awaitTermination(3000L, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// ignore
					}
					Thread.interrupted();

					try {
						// attempt to close the chunked output, since this will make us a well-behaved client
						// by sending the closing chunk.
						chunkedRequestOut.close();
					} catch (Throwable t) {
						// ignore
					}
				}
			} finally {
				socket.close();
			}
		} catch (ConnectException e) {
			getLogger().error(e.getMessage(), e);
			throw new CommandException(-1, "Service temporarily unavailable");
		} catch (IOException e) {
			getLogger().warn(e.getMessage(), e);
			throw new CommandException(-1, e.getMessage());
		}
	}

	private void processData(MultiplexingInputStream input, PacketType packetType, int dataSize) throws IOException,
			CommandException {
		if (dataSize < 1) {
			throw new IllegalArgumentException();
		}

		OutputStream target = null;
		switch (packetType) {
		case STDOUT: // stdout
			target = out;
			break;
		case STDERR: // stderr
			target = err;
			break;
		case EXIT_CODE: // exit code
			break;
		default:
			throw new ProtocolException("Expected indicator but got " + packetType);
		}

		if (target != null) {
			int bytesToRead = dataSize;
			byte[] buffer = new byte[Math.min(bufferSize, bytesToRead)];
			int bytesRead = 0;
			while (bytesRead < bytesToRead) {
				int read = input.read(buffer, 0, Math.min(buffer.length, bytesToRead - bytesRead));
				if (read < 1) {
					throw new IOException("Unexpected EOF");
				}

				target.write(buffer, 0, read);
				bytesRead += read;
			}
			target.flush();
		} else {
			int exitCode = input.readExitCode();
			throw new CommandException(exitCode);
		}

	}

	private void readHttpResponse(InputStream proxyInput) throws IOException, CommandException {
		String statusLineText = HttpParser.readLine(proxyInput, HTTP_ENTITY_CHARSET);
		StatusLine statusLine = new StatusLine(statusLineText);
		if (statusLine.getStatusCode() != HttpServletResponse.SC_OK) {
			String message = Integer.toString(statusLine.getStatusCode());
			String reasonPhrase = statusLine.getReasonPhrase();
			if (reasonPhrase != null && !reasonPhrase.isEmpty()) {
				message += ": " + reasonPhrase;
			}
			throw new CommandException(-1, message);
		}
		Header[] parsedHeaders = HttpParser.parseHeaders(proxyInput, HTTP_ENTITY_CHARSET);
		HeaderGroup headerGroup = new HeaderGroup();
		headerGroup.setHeaders(parsedHeaders);

		Header transferEncoding = headerGroup.getFirstHeader("Transfer-Encoding");
		if (transferEncoding == null || !transferEncoding.getValue().equals("chunked")) {
			throw new IOException("Expected Transfer-Encoding of \"chunked\" but received " + transferEncoding);
		}
		Header contentType = headerGroup.getFirstHeader("Content-Type");
		if (contentType == null || !contentType.getValue().equals(MIME_TYPE_APPLICATION_OCTET_STREAM)) {
			throw new IOException("Unexpected Content-Type " + contentType);
		}
	}

	private void emitHeaders(OutputStream proxyOut, HeaderGroup headers) throws IOException {
		for (Header header : headers.getAllHeaders()) {
			String headerHttpFormat = header.toExternalForm();
			proxyOut.write(headerHttpFormat.getBytes(HTTP_ENTITY_CHARSET));
		}
		// close headers
		proxyOut.write("\r\n".getBytes(HTTP_ENTITY_CHARSET));
	}

	private HeaderGroup computeHeaders(URI targetUri) {
		HeaderGroup headers = new HeaderGroup();
		String host = targetUri.getHost();
		int port = targetUri.getPort();
		String hostHeaderValue = host;
		if (port != 80) {
			hostHeaderValue += ":" + port;
		}
		headers.addHeader(new Header("Host", hostHeaderValue));
		headers.addHeader(new Header("Transfer-Encoding", "chunked"));
		headers.addHeader(new Header("Content-Type", MIME_TYPE_APPLICATION_OCTET_STREAM));
		headers.addHeader(new Header("Accept", MIME_TYPE_APPLICATION_OCTET_STREAM));
		String remoteAddress = session.getIoSession().getRemoteAddress().toString();
		headers.addHeader(new Header("X-Forwarded-For", remoteAddress));
		// TODO: http://en.wikipedia.org/wiki/X-Forwarded-For
		return headers;
	}

	private void emitHttpRequestLine(OutputStream proxyOut, URI targetUri) throws IOException {
		if (targetUri.getQuery() != null) {
			// not supported
			throw new IllegalStateException();
		}
		String requestLine = "POST " + targetUri.getPath() + " HTTP/1.1\r\n";
		proxyOut.write(requestLine.getBytes(HTTP_ENTITY_CHARSET));
	}

	/**
	 * compute the proxy request path. For example, "/scm/"+path
	 * 
	 * @return a path that corresponds to {@link ProjectServiceService#findServiceByUri(String, String)}
	 */
	protected abstract String computeRequestPath(String path);

	protected String computeCommandPath() throws CommandException {
		if (args.size() != 1) {
			throw new CommandException(-1, "Unrecognized command arguments");
		}
		return args.get(0);
	}

	@Override
	protected AbstractInteractiveProxyCommand clone() {
		AbstractInteractiveProxyCommand copy = (AbstractInteractiveProxyCommand) super.clone();
		copy.executorService = executorService;
		copy.projectServiceService = projectServiceService;
		return copy;
	}

}
