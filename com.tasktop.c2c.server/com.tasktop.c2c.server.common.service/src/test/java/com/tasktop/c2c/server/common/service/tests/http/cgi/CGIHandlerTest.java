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
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.tasktop.c2c.server.common.service.web.cgi.CGIHandler;


public class CGIHandlerTest {
	public class DelegatingServletOutputStream extends ServletOutputStream {

		private OutputStream delegate;

		public DelegatingServletOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}

		public void write(int b) {
			try {
				delegate.write(b);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private JUnit4Mockery context = new JUnit4Mockery();

	private Process mockProcess;
	private CGIHandler handler;

	private ByteArrayOutputStream cgiProcessOutput = new ByteArrayOutputStream(1024);
	private InputStream cgiProcessInput = new ByteArrayInputStream(new byte[0]);
	private InputStream cgiProcessErrorInput = new ByteArrayInputStream(new byte[0]);

	private MockHttpServletRequest request;

	private HttpServletResponse response;

	private ByteArrayOutputStream responseOutput = new ByteArrayOutputStream();

	@Before
	public void before() throws IOException {
		context.setImposteriser(ClassImposteriser.INSTANCE);
		mockProcess = context.mock(Process.class);
		handler = new CGIHandler() {
			protected Process createProcess() throws java.io.IOException {
				return mockProcess;
			}

			protected void handleException(java.util.concurrent.ExecutionException e) {
				throw new RuntimeException(e);
			}
		};
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/a/b/c");
		this.request = request;

		response = context.mock(HttpServletResponse.class);

		handler.setCommand("testCgi");
		File tempFile = File.createTempFile("test", ".tmp");
		handler.setWorkingDirectory(tempFile.getParentFile());
		tempFile.delete();

	}

	@Test
	public void testRequestHeaders() throws IOException {

		request.addHeader("Content-Length", "1");
		request.addHeader("Foo", "Bar");

		context.checking(new Expectations() {
			{
				allowing(mockProcess).getOutputStream();
				will(returnValue(cgiProcessOutput));
				allowing(mockProcess).getInputStream();
				will(returnValue(cgiProcessInput));
				allowing(mockProcess).getErrorStream();
				will(returnValue(cgiProcessErrorInput));
				oneOf(mockProcess).destroy();
				ignoring(response);
			}
		});
		handler.service(request, response);

		context.assertIsSatisfied();

		Assert.assertEquals("1", handler.getEnvironment().get("CONTENT_LENGTH"));
		Assert.assertEquals("Bar", handler.getEnvironment().get("HTTP_FOO"));
	}

	@Test
	public void testResponseWithStatus() throws IOException {
		setupCgiOutput("Status: 200 OK\nContent-Length: 2\nContent-Type: text/plain\n\n12");

		context.checking(new Expectations() {
			{
				allowing(mockProcess).getOutputStream();
				will(returnValue(cgiProcessOutput));
				allowing(mockProcess).getInputStream();
				will(returnValue(cgiProcessInput));
				allowing(mockProcess).getErrorStream();
				will(returnValue(cgiProcessErrorInput));
				oneOf(response).getOutputStream();
				will(returnValue(new DelegatingServletOutputStream(responseOutput)));
				oneOf(response).setHeader("Content-Length", "2");
				oneOf(response).setHeader("Content-Type", "text/plain");
				oneOf(mockProcess).destroy();
				oneOf(response).setStatus(200);
			}
		});
		handler.service(request, response);

		context.assertIsSatisfied();

		Assert.assertEquals("12", responseOutput.toString());

	}

	protected void setupCgiOutput(String cgiProgramOutput) {
		cgiProcessInput = new ByteArrayInputStream(cgiProgramOutput.getBytes());
	}

	@Test
	public void testClone() {
		File workingDirectory = new File("Foo/bar");

		handler.setWorkingDirectory(workingDirectory);
		handler.getEnvironment().put("a", "b");

		CGIHandler copy = handler.clone();
		assertNotSame(handler, copy);
		assertEquals(workingDirectory, copy.getWorkingDirectory());
		assertEquals(handler.getEnvironment(), copy.getEnvironment());
		assertNotSame(handler.getEnvironment(), copy.getEnvironment());
	}
}
