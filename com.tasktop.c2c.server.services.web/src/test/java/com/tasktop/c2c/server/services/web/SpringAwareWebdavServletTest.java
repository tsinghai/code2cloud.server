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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.services.web.SpringAwareWebdavServlet;
import com.tasktop.c2c.server.services.web.TenantAwareWebDavStore;

public class SpringAwareWebdavServletTest {

	private SpringAwareWebdavServlet servlet = null;
	private File tenantFolder = null;
	private File tempFolder = null;
	private File tempFile = null;
	private File evilFolder = null;
	private File evilFile = null;

	@Before
	public void setUp() throws Exception {

		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		tenantFolder = new File(tempDir, "identity"); // /tmp/identity
		tenantFolder.mkdir();
		tempFolder = new File(tenantFolder, "folder2"); // /tmp/identity/folder2
		tempFolder.mkdir();
		tempFile = new File(tempFolder, "test.txt"); // /tmp/identity/folder2/test.txt
		tempFile.createNewFile();
		evilFolder = new File(tempDir, "eeeeevilFolder"); // /tmp/evil
		evilFolder.mkdir();
		evilFile = new File(evilFolder, "evil.txt"); // /tmp/evil/evil.txt
		evilFile.createNewFile();

		servlet = new SpringAwareWebdavServlet();
		servlet.setWebdavStore(new TenantAwareWebDavStore(tempDir));
		servlet.setServletContext(new MockServletContext());
		servlet.init();

		DefaultTenant tenant = new DefaultTenant();
		tenant.setData("data");
		tenant.setIdentity(tenantFolder.getName());

		TenancyContextHolder.createEmptyContext();
		TenancyContextHolder.getContext().setTenant(tenant);
	}

	@After
	public void tearDown() throws Exception {
		// Clean up all of our test files and directories.
		evilFile.delete();
		evilFolder.delete();
		tempFile.delete();
		tempFolder.delete();
		tenantFolder.delete();
	}

	@Test
	public void testServiceHttpServletRequestHttpServletResponse_relativePath() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();

		req.setMethod("GET");
		req.setPathInfo("/../" + evilFolder.getName() + "/" + evilFile.getName());

		servlet.service(req, resp);

		assertEquals(404, resp.getStatus());
	}

	@Test
	public void testServiceHttpServletRequestHttpServletResponse_happyPath() throws Exception {

		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse resp = new MockHttpServletResponse();

		req.setMethod("GET");
		req.setPathInfo("/" + tempFolder.getName() + "/" + tempFile.getName());

		servlet.service(req, resp);

		assertEquals(200, resp.getStatus());
	}
}
