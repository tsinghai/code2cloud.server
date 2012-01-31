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
package com.tasktop.c2c.server.web.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpProxy extends WebProxy {
	private static Pattern targetUrlPattern = Pattern.compile("https?://.+");

	private ClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();

	public HttpProxy() {
		((CommonsClientHttpRequestFactory) requestFactory).getHttpClient().getParams()
				.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		headerFilter = new CookieHeaderFilter();
		ExcludeHeaderFilter excludeHeaders = new ExcludeHeaderFilter();
		excludeHeaders.getExcludedRequestHeaders().addAll(Arrays.asList("Connection", "Accept-Encoding"));
		excludeHeaders.getExcludedResponseHeaders().addAll(Arrays.asList("Connection", "Transfer-Encoding"));
		headerFilter.setNext(excludeHeaders);
	}

	@Override
	public boolean canProxyRequest(String targetUrl, HttpServletRequest request) {
		return targetUrlPattern.matcher(targetUrl).matches();
	}

	@Override
	protected void proxy(String targetUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {

		ClientHttpResponse proxyResponse = null;
		try {
			ClientHttpRequest proxyRequest = createProxyRequest(targetUrl, request);
			proxyResponse = proxyRequest.execute();
			copyProxyReponse(proxyResponse, response);
		} catch (ConnectException e) {
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Internal connection issue");
			return;
		} finally {
			if (proxyResponse != null) {
				proxyResponse.close();
			}
		}
	}

	private String uriEncode(String url) {
		return url.replace(" ", "%20");
	}

	ClientHttpRequest createProxyRequest(String targetUrl, HttpServletRequest request) throws IOException {
		URI targetUri;
		try {
			targetUri = new URI(uriEncode(targetUrl));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		// FIXME this prevents proxying webDav methods
		ClientHttpRequest proxyRequest = requestFactory.createRequest(targetUri,
				HttpMethod.valueOf(request.getMethod()));

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			Enumeration<String> headerVals = request.getHeaders(headerName);
			while (headerVals.hasMoreElements()) {
				String headerValue = headerVals.nextElement();
				headerValue = headerFilter.processRequestHeader(headerName, headerValue);
				if (headerValue != null) {
					proxyRequest.getHeaders().add(headerName, headerValue);
				}

			}
		}

		if (request.getContentLength() > 0) {
			// FIXME: do all clients send a content length?
			copy(request.getInputStream(), proxyRequest.getBody());
		}

		return proxyRequest;
	}

	void copyProxyReponse(ClientHttpResponse proxyResponse, HttpServletResponse response) throws IOException {
		response.reset();
		response.setStatus(proxyResponse.getStatusCode().value());
		copyProxyHeaders(proxyResponse.getHeaders(), response);
		copy(proxyResponse.getBody(), response.getOutputStream());
	}

	private void copyProxyHeaders(HttpHeaders headers, HttpServletResponse response) {
		for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
			String header = headerEntry.getKey();
			for (String value : headerEntry.getValue()) {
				value = headerFilter.processResponseHeader(header, value);
				if (value != null) {
					response.addHeader(header, value);
				}
			}
		}
		response.setContentLength((int) headers.getContentLength());
	}

	private static int RESPONSE_BUFFER_SIZE = 10 * 1024;

	private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		if (inputStream == null) {
			return;
		}
		byte[] buffer = new byte[RESPONSE_BUFFER_SIZE];
		int numRead;
		while ((numRead = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, numRead);
		}

	}

	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		this.requestFactory = requestFactory;
	}

}
