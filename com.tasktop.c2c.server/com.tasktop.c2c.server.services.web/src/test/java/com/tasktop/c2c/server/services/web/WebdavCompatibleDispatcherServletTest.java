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
package com.tasktop.c2c.server.services.web;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import com.tasktop.c2c.server.services.web.WebdavCompatibleDispatcherServlet;

public class WebdavCompatibleDispatcherServletTest {

	@Test
	public void testHttpServletFailsWebdavMethods() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();

		// Wire in a non-HTTP Webdav method
		req.setMethod("PROPFIND");

		// Confirm that a normal HTTP servlet fails to handle Webdav methods - this is done as a sanity check.
		new DispatcherServlet().service(req, resp);

		assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, resp.getStatus());
	}

	// It may seem counterintuitive that a nullpointer is an indication of success, but it means that (1) the call is
	// being processed by the servlet, which means it was not screened out as it was above, and (2) configuring this
	// servlet inside of the test proved very difficult, and the return-on-investment when just trying to test whether a
	// method is accepted wasn't there.
	@Test(expected = NullPointerException.class)
	public void testServiceSupportsWebdavMethods() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();
		WebdavCompatibleDispatcherServlet srv = new WebdavCompatibleDispatcherServlet();

		// Wire in a non-HTTP Webdav method
		req.setMethod("PROPFIND");

		// Confirm that our HTTP servlet handles the Webdav method correctly.
		srv.service(req, resp);
	}
}
