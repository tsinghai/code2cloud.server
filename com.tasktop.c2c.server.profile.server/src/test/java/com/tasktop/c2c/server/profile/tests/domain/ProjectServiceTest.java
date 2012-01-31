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
package com.tasktop.c2c.server.profile.tests.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;


/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class ProjectServiceTest {

	private ProjectService projectService;
	private ServiceHost serviceHost;

	@Before
	public void setUp() {
		serviceHost = new ServiceHost();
		serviceHost.setInternalNetworkAddress("10.0.0.34");

		projectService = new ProjectService();
		projectService.setServiceHost(serviceHost);
		serviceHost.getProjectServices().add(projectService);
	}

	@Test
	public void testComputeInternalProxyBaseUri_WithAJP() {
		projectService.setAjpPort(123);
		projectService.setInternalPort(8080);
		projectService.setInternalUriPrefix("/foo");

		String ajpProxyUri = projectService.computeInternalProxyBaseUri(true);
		assertEquals("ajp://10.0.0.34:123/foo", ajpProxyUri);

		String httpProxyUri = projectService.computeInternalProxyBaseUri(false);
		assertEquals("http://10.0.0.34:8080/foo", httpProxyUri);
	}

}
