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
package com.tasktop.c2c.server.web.proxy.ajp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool.KeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.web.proxy.HeaderFilter;

public class AjpProtocol {

	private enum HttpMethod {
		OPTIONS((byte) 1), //
		GET((byte) 2), //
		HEAD((byte) 3), //
		POST((byte) 4), //
		PUT((byte) 5), //
		DELETE((byte) 6), //
		TRACE((byte) 7), //
		PROPFIND((byte) 8), //
		PROPMATCH((byte) 9), //
		MKCOL((byte) 10), //
		COPY((byte) 11), //
		MOVE((byte) 12), //
		LOCK((byte) 13), //
		UNLOCK((byte) 14), //
		ACL((byte) 15), //
		REPORT((byte) 16), //
		VERSION_CONTROL((byte) 17), //
		CHECKIN((byte) 18), //
		CHECKOUT((byte) 19), //
		UNCHECKOUT((byte) 20), //
		SEARCH((byte) 21), //
		MKWORKSPACE((byte) 22), //
		UPDATE((byte) 23), //
		LABEL((byte) 24), //
		MERGE((byte) 25), //
		BASELINE_CONTROL((byte) 26), //
		MKACTIVITY((byte) 27), //
		;

		private final byte code;

		private HttpMethod(byte code) {
			this.code = code;
		}

		public String getHeaderName() {
			if (this == VERSION_CONTROL) {
				return name().replace('_', '-');
			}
			return name();
		}
	}

	private enum Type {
		REQUEST_FORWARD((byte) 2), //
		SEND_BODY_CHUNK((byte) 3), //
		SEND_HEADERS((byte) 4), //
		END_RESPONSE((byte) 5), //
		GET_BODY_CHUNK((byte) 6), //
		SHUTDOWN((byte) 7), //
		PING((byte) 8), //
		CPONG((byte) 9), //
		CPING((byte) 10);

		private final byte code;

		private Type(byte packetType) {
			this.code = packetType;
		}

		public static Type fromCode(byte code) {
			for (Type type : Type.values()) {
				if (type.code == code) {
					return type;
				}
			}
			throw new IllegalArgumentException("Unexpected packet type " + ((int) code));
		}

	}

	private enum HttpRequestHeader {
		ACCEPT("Accept", 0xA001), //
		ACCEPT_CHARSET("Accept-Charset", 0xA002), //
		ACCEPT_ENCODING("Accept-Encoding", 0xA003), //
		ACCEPT_LANGUAGE("Accept-Language", 0xA004), //
		AUTHORIZATION("Authorization", 0xA005), //
		CONNECTION("Connection", 0xA006), //
		CONTENT_TYPE("Content-Type", 0xA007), //
		CONTENT_LENGTH("Content-Length", 0xA008), //
		COOKIE("Cookie", 0xA009), //
		COOKIE2("Cookie2", 0xA00A), //
		HOST("Host", 0xA00B), //
		PRAGMA("Pragma", 0xA00C), //
		REFERER("Referer", 0xA00D), //
		USER_AGENT("User-Agent", 0xA00E), //
		;

		private final String headerName;
		private final int code;
		private static Map<String, HttpRequestHeader> valueByHeaderName = new HashMap<String, HttpRequestHeader>();
		static {
			for (HttpRequestHeader header : HttpRequestHeader.values()) {
				valueByHeaderName.put(header.headerName.toLowerCase(), header);
			}
		}

		private HttpRequestHeader(String headerName, int code) {
			this.headerName = headerName;
			this.code = code;
		}

		/**
		 * get the HTTP header by its header name
		 * 
		 * @return the value, or null if there is no mapping
		 */
		public static HttpRequestHeader fromHeaderName(String headerName) {
			return valueByHeaderName.get(headerName.toLowerCase());
		}

	}

	private enum HttpResponseHeader {
		CONTENT_TYPE("Content-Type", 0xA001), //
		CONTENT_LANGUAGE("Content-Language", 0xA002), //
		CONTENT_LENGTH("Content-Length", 0xA003), //
		DATE("Date", 0xA004), //
		LAST_MODIFIED("Last-Modified", 0xA005), //
		LOCATION("Location", 0xA006), //
		SET_COOKIE("Set-Cookie", 0xA007), //
		SET_COOKIE2("Set-Cookie2", 0xA008), //
		SERVLET_ENGINE("Servlet-Engine", 0xA009), //
		STATUS("Status", 0xA00A), //
		WWW_AUTHENTICATE("WWW-Authenticate", 0xA00B), //
		;

		private final String headerName;
		private final int code;

		private HttpResponseHeader(String headerName, int code) {
			this.headerName = headerName;
			this.code = code;
		}

		/**
		 * get the HTTP header by its header name
		 * 
		 * @return the value, or null if there is no mapping
		 */
		public static HttpResponseHeader fromCode(int code) {
			for (HttpResponseHeader header : HttpResponseHeader.values()) {
				if (header.code == code) {
					return header;
				}
			}

			throw new IllegalArgumentException("Unexpected header code " + code);
		}

	}

	private enum Attribute {
		REMOTE_USER((byte) 0x03), QUERY_STRING((byte) 0x05);

		private final byte code;

		private Attribute(byte code) {
			this.code = code;
		}

	}

	private static final Logger LOG = LoggerFactory.getLogger(AjpProtocol.class.getName());

	private String proxyHost;
	private int proxyPort;

	private KeyedObjectPool socketPool;

	private HeaderFilter headerFilter = new HeaderFilter();

	private boolean shareConnections = false; // FIXME workaround for task 1008

	private HttpMethod computeMethod(String methodHeader) {
		for (HttpMethod method : HttpMethod.values()) {
			if (method.getHeaderName().equalsIgnoreCase(methodHeader)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Unsupported method " + methodHeader);
	}

	public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
		debug(request, "forward");

		Packet packet = new Packet();
		packet.reset();
		// AJP13_FORWARD_REQUEST
		packet.write(Type.REQUEST_FORWARD.code);
		packet.write(computeMethod(request.getMethod()).code);
		packet.write(request.getProtocol());
		packet.write(request.getRequestURI());
		packet.write(request.getRemoteAddr());
		packet.write(request.getRemoteAddr());
		packet.write(request.getServerName());
		packet.write(request.getServerPort());
		packet.write(request.isSecure());

		// request headers
		Map<String, String> headers = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement().toString();
			String headerValue = request.getHeader(headerName);
			headerValue = headerFilter.processRequestHeader(headerName, headerValue);
			if (headerValue != null) {
				headers.put(headerName, headerValue);
			}
		}
		packet.write(headers.size());
		for (Map.Entry<String, String> header : headers.entrySet()) {
			HttpRequestHeader headerType = HttpRequestHeader.fromHeaderName(header.getKey());
			if (headerType != null) {
				packet.write(headerType.code);
			} else {
				packet.write(header.getKey());
			}
			String headerValue = header.getValue();
			packet.write(headerValue == null ? "" : headerValue);
		}

		// request attributes
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			packet.write(Attribute.REMOTE_USER.code);
			packet.write(authentication.getName());
		}

		String queryString = request.getQueryString();
		if (queryString != null) {
			packet.write(Attribute.QUERY_STRING.code);
			packet.write(queryString);
		}

		// packet terminator
		packet.write((byte) 0xff);

		final Object socketKey = new AjpPoolableConnectionFactory.Key(proxyHost, proxyPort);
		Socket connection;
		try {
			connection = allocateSocket(socketKey);
			debug("allocated", connection);

		} catch (Exception e) {
			LOG.error(String.format("Cannot connect to %s:%s", proxyHost, proxyPort), e);
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}
		boolean invalidate = true;
		try {
			OutputStream outputStream = connection.getOutputStream();
			InputStream inputStream = connection.getInputStream();
			packet.write(outputStream);
			packet.reset();

			int bytesWritten = 0;

			int contentLength = request.getContentLength();
			if (contentLength == -1) { // Unknown content length
				contentLength = Integer.MAX_VALUE;
			}
			ServletInputStream requestInput = request.getInputStream();

			OutputStream responseOutput = null;
			boolean reuse = false;

			if (request.getHeader("Content-Length") != null) {
				bytesWritten += processRequestBody(packet, outputStream, bytesWritten, contentLength, requestInput,
						contentLength);
				debug("sent [" + bytesWritten + "] initial body bytes", connection);
			}

			for (;; packet.reset()) {
				debug("reading packet", connection);
				packet.read(inputStream);

				Type packetType = Type.fromCode(packet.readByte());
				debug("received " + packetType, connection);
				if (packetType == Type.END_RESPONSE) {
					reuse = packet.readBoolean();
					break;
				}
				switch (packetType) {
				case GET_BODY_CHUNK:
					int requestedSize = packet.readInt();
					packet.reset();
					int chunkSize = processRequestBody(packet, outputStream, bytesWritten, contentLength, requestInput,
							requestedSize);
					bytesWritten += chunkSize;
					debug("sent [" + chunkSize + "] bytes of body check", connection);
					break;
				case SEND_HEADERS: {
					response.reset();
					int httpStatusCode = packet.readInt();
					packet.readString(); // status message, not used
					response.setStatus(httpStatusCode);
					int headerCount = packet.readInt();
					for (int x = 0; x < headerCount; ++x) {
						byte b = packet.readByte();
						packet.unreadByte();
						String headerName;
						if (b == ((byte) 0xA0)) {
							int headerCode = packet.readInt();
							headerName = HttpResponseHeader.fromCode(headerCode).headerName;
						} else {
							headerName = packet.readString();
						}
						String headerValue = packet.readString();
						headerValue = headerFilter.processResponseHeader(headerName, headerValue);
						if (headerValue != null) {
							response.setHeader(headerName, headerValue);
						}
					}
				}
					break;
				case SEND_BODY_CHUNK:
					if (responseOutput == null) {
						responseOutput = response.getOutputStream();
					}
					packet.copy(responseOutput);
					break;
				}
			}

			// ORDER DEPENDENCY: this should come last
			invalidate = !reuse;

			if (responseOutput != null) {
				responseOutput.close();
			}
		} finally {
			if (!shareConnections) {
				invalidate = true;
			}
			deallocateSocket(socketKey, connection, invalidate);
			debug("released " + (invalidate ? "invalidate" : "reuse"), connection);
		}
	}

	private void debug(String string, Socket connection) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("connection " + System.identityHashCode(connection) + ": " + string);
		}
	}

	private void debug(HttpServletRequest request, String activity) {
		if (LOG.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			out.println(activity + " - " + request.getRequestURL());
			out.println("\tMethod: " + request.getMethod());

			@SuppressWarnings("rawtypes")
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement().toString();
				String headerValue = request.getHeader(headerName);
				headerValue = headerFilter.processRequestHeader(headerName, headerValue);
				if (headerValue != null) {
					out.println("\theader: " + headerName + ": " + headerValue);
				}
			}
			out.close();
			LOG.debug(writer.toString());
		}
	}

	private static final byte[] ZERO_CONTENTS_RESPONSE = new byte[] { 0x12, 0x34, 0x00, 0x00 };

	private int processRequestBody(Packet packet, OutputStream outputStream, int bytesWritten, int contentLength,
			ServletInputStream requestInput, int requestedSize) throws IOException {
		int bytesInPacket = 0;

		// This is a special packet and has no header code

		bytesInPacket = packet.stream(requestInput, Math.min(contentLength - bytesWritten, requestedSize));

		// Send even if nothing read
		if (bytesInPacket == 0) {
			outputStream.write(ZERO_CONTENTS_RESPONSE);
		} else {
			packet.write(outputStream);
		}

		bytesWritten += bytesInPacket;
		packet.reset();

		return bytesInPacket;
	}

	protected void deallocateSocket(Object key, Socket connection, boolean invalidate) {
		try {
			if (invalidate) {
				socketPool.invalidateObject(key, connection);
			} else {
				socketPool.returnObject(key, connection);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(AjpProtocol.class).error(e.getMessage(), e);
		}
	}

	protected Socket allocateSocket(Object key) throws Exception {
		return (Socket) socketPool.borrowObject(key);
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public KeyedObjectPool getSocketPool() {
		return socketPool;
	}

	public void setSocketPool(KeyedObjectPool socketPool) {
		this.socketPool = socketPool;
	}

	public HeaderFilter getHeaderFilter() {
		return headerFilter;
	}

	public void setHeaderFilter(HeaderFilter headerFilter) {
		this.headerFilter = headerFilter;
	}

}
