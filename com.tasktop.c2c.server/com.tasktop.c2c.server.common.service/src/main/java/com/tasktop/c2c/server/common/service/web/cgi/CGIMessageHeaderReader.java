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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A means of reading a CGI response message-header per RFC 3875 section 6. Guarantees that all CGI header bytes are
 * read completely, without reading any message-body bytes.
 * 
 * Usage:
 * 
 * <pre>
 * <code>
 * CGIMessageHeaderReader headerReader = ...;
 * headerReader.readHeader();
 * int statusCode = headerReader.getHttpStatusCode();
 * 
 * 
 * </code>
 * </pre>
 * 
 * @author David Green
 */
public class CGIMessageHeaderReader {

	private static final Pattern HEADER_PATTERN = Pattern.compile("([^\\s:]+):\\s*(.*)");
	private static final Pattern STATUS_PATTERN = Pattern.compile("(\\d+)(?:\\s+(.*))");

	private final InputStream input;

	private Map<String, String> headers = new HashMap<String, String>();

	private int httpStatusCode;
	private String httpStatusReasonPhrase;

	public CGIMessageHeaderReader(InputStream input) {
		this.input = input;
	}

	public void readHeader() throws IOException {
		StringBuilder currentLine = new StringBuilder();
		int i;
		while ((i = input.read()) != -1) {
			if (i == '\r') {
				continue;
			}
			if (i == '\n') {
				String headerLineText = currentLine.toString();
				if (headerLineText.length() == 0) {
					break;
				}
				processHeaderLine(headerLineText);
				currentLine.delete(0, currentLine.length());
			} else {
				currentLine.append((char) i);
			}
		}
	}

	private void processHeaderLine(String headerLineText) {
		// this is easy: per RFC 3875 section 6.3 no continuation lines, so the whole value is on one line
		Matcher matcher = HEADER_PATTERN.matcher(headerLineText);
		if (matcher.matches()) {
			String headerField = matcher.group(1);
			String fieldValue = matcher.group(2);
			if (headerField.equalsIgnoreCase("Status")) {
				Matcher statusMatcher = STATUS_PATTERN.matcher(fieldValue);
				if (statusMatcher.matches()) {
					try {
						httpStatusCode = Integer.parseInt(statusMatcher.group(1));
						httpStatusReasonPhrase = statusMatcher.group(2);
					} catch (NumberFormatException e) {
						throw new IllegalStateException("Malformed CGI response: " + fieldValue);
					}
				} else {
					throw new IllegalStateException("Malformed CGI response: " + fieldValue);
				}
			} else {
				headers.put(headerField, fieldValue);
			}
		} else {
			throw new IllegalStateException("Malformed CGI response: " + headerLineText);
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getHttpStatusCode() {
		// section 6.2.1, status 200 is assumed for document response
		return httpStatusCode == 0 ? 200 : httpStatusCode;
	}

	public String getHttpStatusReasonPhrase() {
		return httpStatusReasonPhrase;
	}
}
