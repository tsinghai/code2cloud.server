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
package com.tasktop.c2c.server.common.service.tests.ajp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

public class TestServlet extends HttpServlet implements HttpRequestHandler {

	public static final class Payload {
		int responseCode;
		Map<String, String> responseHeaders = new HashMap<String, String>();
		String characterContent;
		byte[] binaryContent;
		Map<String, String> sessionVariables = new HashMap<String, String>();

		public Payload() {
		}

		public Payload(int responseCode, String characterContent) {
			this.responseCode = responseCode;
			this.characterContent = characterContent;
		}

		public Payload(int responseCode, byte[] binaryContent) {
			super();
			this.responseCode = responseCode;
			this.binaryContent = binaryContent;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public Map<String, String> getResponseHeaders() {
			return responseHeaders;
		}

		public Map<String, String> getSessionVariables() {
			return sessionVariables;
		}

		public void setSessionVariables(Map<String, String> sessionVariables) {
			this.sessionVariables = sessionVariables;
		}

		public String getCharacterContent() {
			return characterContent;
		}

		public byte[] getBinaryContent() {
			return binaryContent;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public void setResponseHeaders(Map<String, String> responseHeaders) {
			this.responseHeaders = responseHeaders;
		}

		public void setCharacterContent(String characterContent) {
			this.characterContent = characterContent;
		}

		public void setBinaryContent(byte[] binaryContent) {
			this.binaryContent = binaryContent;
		}

	}

	public static class Request {
		private Map<String, String[]> parameters = new HashMap<String, String[]>();
		private Map<String, String> headers = new HashMap<String, String>();
		private byte[] input;
		private String requestURI;
		private String queryString;
		private boolean newSession;
		private Map<String, Object> sessionAttributes = new HashMap<String, Object>();
		private String sessionId;

		public Request(HttpServletRequest request) throws IOException {
			ServletInputStream input = request.getInputStream();
			int i;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((i = input.read()) != -1) {
				out.write(i);
			}
			this.input = out.toByteArray();

			parameters.putAll((Map<String, String[]>) request.getParameterMap());
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				headers.put(headerName, request.getHeader(headerName));
			}
			requestURI = request.getRequestURI();
			queryString = request.getQueryString();
			newSession = request.getSession().isNew();
			Enumeration<String> sessionAttributeNames = request.getSession().getAttributeNames();
			while (sessionAttributeNames.hasMoreElements()) {
				String attributeName = sessionAttributeNames.nextElement();
				sessionAttributes.put(attributeName, request.getSession().getAttribute(attributeName));
			}
			sessionId = request.getSession().getId();
		}

		public String getSessionId() {
			return sessionId;
		}

		public boolean isNewSession() {
			return newSession;
		}

		public Map<String, Object> getSessionAttributes() {
			return sessionAttributes;
		}

		public byte[] getInput() {
			return input;
		}

		public void setInput(byte[] input) {
			this.input = input;
		}

		public String getRequestURI() {
			return requestURI;
		}

		public String getQueryString() {
			return queryString;
		}

		public Map<String, String[]> getParameters() {
			return parameters;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}
	}

	private static Payload responsePayload;
	private static Request lastRequest;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		Payload payload = responsePayload;
		if (payload == null) {
			throw new IllegalStateException();
		}
		if (!payload.getSessionVariables().isEmpty()) {
			for (Entry<String, String> variable : payload.getSessionVariables().entrySet()) {
				request.getSession().setAttribute(variable.getKey(), variable.getValue());
			}
		}
		lastRequest = new Request(request);
		response.setStatus(payload.responseCode);
		for (Map.Entry<String, String> header : payload.responseHeaders.entrySet()) {
			response.setHeader(header.getKey(), header.getValue());
		}
		if (payload.binaryContent != null) {
			ServletOutputStream outputStream = response.getOutputStream();
			try {
				outputStream.write(payload.binaryContent);
			} finally {
				outputStream.close();
			}
		} else if (payload.characterContent != null) {
			PrintWriter writer = response.getWriter();
			try {
				writer.write(payload.characterContent.toCharArray());
			} finally {
				writer.close();
			}
		}
	}

	public static Payload getResponsePayload() {
		return responsePayload;
	}

	public static void setResponsePayload(Payload responsePayload) {
		TestServlet.responsePayload = responsePayload;
	}

	public static Request getLastRequest() {
		return lastRequest;
	}

	public static void setLastRequest(Request lastRequest) {
		TestServlet.lastRequest = lastRequest;
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		service(request, response);
	}

}
