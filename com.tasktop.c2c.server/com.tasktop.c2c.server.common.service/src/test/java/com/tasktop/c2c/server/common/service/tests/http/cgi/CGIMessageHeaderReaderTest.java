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
package com.tasktop.c2c.server.common.service.tests.http.cgi;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.tasktop.c2c.server.common.service.web.cgi.CGIMessageHeaderReader;


public class CGIMessageHeaderReaderTest {

	@Test
	public void testSimpleCGIResponse() throws IOException {
		String response = "Status: 200 OK testing 123\nContent-Type: text/plain\n\n";
		CGIMessageHeaderReader headerReader = createHeaderReader(response);

		headerReader.readHeader();

		assertEquals(200, headerReader.getHttpStatusCode());
		assertEquals("OK testing 123", headerReader.getHttpStatusReasonPhrase());
		assertEquals("text/plain", headerReader.getHeaders().get("Content-Type"));
	}

	@Test
	public void testSimpleCGIResponseNoStatus() throws IOException {
		String response = "Content-Type: text/plain\n\n";
		CGIMessageHeaderReader headerReader = createHeaderReader(response);

		headerReader.readHeader();

		assertEquals(200, headerReader.getHttpStatusCode());
		assertEquals("text/plain", headerReader.getHeaders().get("Content-Type"));
	}

	@Test
	public void testCGIResponseBodyNotRead() throws IOException {
		String response = "Status: 200 OK testing 123\nContent-Type: text/unknown\n\nStart of content";
		InputStream cgiOutput = createCGIResponseOutput(response);
		CGIMessageHeaderReader headerReader = new CGIMessageHeaderReader(cgiOutput);

		headerReader.readHeader();

		assertEquals(200, headerReader.getHttpStatusCode());
		assertEquals("OK testing 123", headerReader.getHttpStatusReasonPhrase());
		assertEquals("text/unknown", headerReader.getHeaders().get("Content-Type"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(cgiOutput));

		assertEquals("Start of content", reader.readLine());
	}

	protected CGIMessageHeaderReader createHeaderReader(String response) throws IOException {
		return new CGIMessageHeaderReader(createCGIResponseOutput(response));
	}

	protected InputStream createCGIResponseOutput(String response) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(response.getBytes("utf-8"));
	}
}
