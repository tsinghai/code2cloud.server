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
package com.tasktop.c2c.server.profile.tests.service;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.profile.service.ProjectArtifactServiceClient;
import com.tasktop.c2c.server.profile.web.ui.server.ProjectArtifactServiceController;

@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml", "/applicationContext-testSecurity.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ProjectArtifactServiceClientTest extends ProjectArtifactServiceTest {

	@Autowired
	private ProjectArtifactServiceClient serviceClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WebApplicationContainerBean container;

	@Override
	@Before
	public void setup() {
		super.setup();

		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				ProjectArtifactServiceClientTest.class));
		container.start();
		serviceClient.setBaseUrl(container.getBaseUrl() + "artifacts");

		ProjectArtifactServiceController.setLastInstantiatedService(projectArtifactService);
		serviceClient.setRestTemplate(restTemplate);
		super.projectArtifactService = serviceClient;

	}

	@After
	public void after() {
		if (container != null) {
			container.stop();
			container = null;
		}
	}

}
