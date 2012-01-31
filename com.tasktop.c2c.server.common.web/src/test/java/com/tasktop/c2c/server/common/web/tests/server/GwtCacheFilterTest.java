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
package com.tasktop.c2c.server.common.web.tests.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.tasktop.c2c.server.common.web.server.GwtCacheFilter;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class GwtCacheFilterTest {

	@Test
	public void test() throws IOException, ServletException {
		Date now = new Date();
		GwtCacheFilter filter = new GwtCacheFilter();
		HttpServletRequest request = new MockHttpServletRequest("GET", "/profile.nocache.js");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(request, response, chain);
		Assert.assertTrue(response.containsHeader("Expires"));
		Assert.assertTrue(now.after(new Date((Long) response.getHeader("Expires"))));

		request = new MockHttpServletRequest("GET", "/profile.XXXXXXX.cache.js");
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();
		filter.doFilter(request, response, chain);
		Assert.assertTrue(response.containsHeader("Expires"));
		Assert.assertTrue(now.before(new Date((Long) response.getHeader("Expires"))));

	}
}
