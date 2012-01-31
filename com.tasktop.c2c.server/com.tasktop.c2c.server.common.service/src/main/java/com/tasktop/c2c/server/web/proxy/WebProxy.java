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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public abstract class WebProxy {
	private static class ArrayEnumeration<T> implements Enumeration<T> {

		private final T[] values;
		private int offset = 0;

		public ArrayEnumeration(T... values) {
			this.values = values;
		}

		@Override
		public boolean hasMoreElements() {
			return offset < values.length;
		}

		@Override
		public T nextElement() {
			try {
				return values[offset++];
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

	}

	public class FormSubmissionHttpServletRequest extends HttpServletRequestWrapper {

		private ServletInputStream inputStream;
		private BufferedReader reader;
		private final byte[] content;

		public FormSubmissionHttpServletRequest(HttpServletRequest request, byte[] formContent) {
			super(request);
			this.content = formContent;
		}

		@Override
		public int getContentLength() {
			return content.length;
		}

		@Override
		public String getHeader(String name) {
			if (name.equalsIgnoreCase("content-length")) {
				return Integer.toString(content.length);
			} else if (name.equalsIgnoreCase("content-type")) {
				return getContentType();
			}
			return super.getHeader(name);
		}

		@Override
		public String getContentType() {
			return "application/x-www-form-urlencoded; charset=UTF-8";
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Enumeration getHeaders(final String name) {
			if (name.equalsIgnoreCase("content-length")) {
				return new ArrayEnumeration(getHeader(name));
			} else if (name.equalsIgnoreCase("content-type")) {
				return new ArrayEnumeration(getContentType());
			}
			return super.getHeaders(name);
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (inputStream == null) {
				inputStream = new ServletInputStream() {

					ByteArrayInputStream in = new ByteArrayInputStream(content);

					@Override
					public int read() throws IOException {
						return in.read();
					}
				};
			}
			return inputStream;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			if (reader == null) {
				reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content), "utf-8"));
			}
			return reader;
		}

	}

	protected HeaderFilter headerFilter = new CookieHeaderFilter();

	public WebProxy() {
	}

	public abstract boolean canProxyRequest(String targetUrl, HttpServletRequest request);

	public final void proxyRequest(String targetUrl, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		byte[] formContent = computeFormContent(request);
		if (formContent != null) {
			request = new FormSubmissionHttpServletRequest(request, formContent);
		}
		proxy(targetUrl, request, response);
	}

	protected abstract void proxy(String targetUrl, HttpServletRequest request, HttpServletResponse response)
			throws IOException;

	public HeaderFilter getHeaderFilter() {
		return headerFilter;
	}

	public void setHeaderFilter(HeaderFilter headerFilter) {
		this.headerFilter = headerFilter;
	}

	/**
	 * Compute the request body content based on the content type.
	 * 
	 * @param request
	 *            the servlet request
	 * @return the content, x-www-form-urlencoded using utf-8, or null if the given request is not x-www-form-urlencoded
	 */
	private byte[] computeFormContent(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType != null) {
			int indexOfSemi = contentType.indexOf(';');
			if (indexOfSemi >= 0) {
				contentType = contentType.substring(0, indexOfSemi);
			}
			contentType = contentType.trim();
			if ("application/x-www-form-urlencoded".equals(contentType)) {
				try {
					StringBuilder buf = new StringBuilder(2048);
					Enumeration<String> names = request.getParameterNames();
					while (names.hasMoreElements()) {
						String name = names.nextElement();
						for (String value : request.getParameterValues(name)) {
							if (buf.length() > 0) {
								buf.append('&');
							}
							buf.append(URLEncoder.encode(name, "utf-8"));
							buf.append('=');
							if (value != null && value.length() > 0) {
								buf.append(URLEncoder.encode(value, "utf-8"));
							}
						}
					}
					return buf.toString().getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return null;
	}
}
