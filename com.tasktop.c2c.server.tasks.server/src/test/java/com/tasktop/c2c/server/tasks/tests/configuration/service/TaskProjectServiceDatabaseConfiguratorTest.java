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
package com.tasktop.c2c.server.tasks.tests.configuration.service;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.configuration.service.ProjectServiceDatabaseConfigurator;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;

/**
 * Verifies that the project task DB is created and schema installed with no Exceptions. This tests the
 * {@link ProjectServiceDatabaseConfigurator} as well as the liquibase schema and initial data files for Task DBs
 * 
 * @author Jennifer Hickey
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
@DirtiesContext
@Ignore("Broken because the migrations need to be mysql and hsql compliant")
public class TaskProjectServiceDatabaseConfiguratorTest {

	@Resource(name = "taskTestDatabaseConfigurer")
	private ProjectServiceDatabaseConfigurator configurator;

	private static final String TEST_PROJ_ID = "taskconfiguratortester";

	@Test
	public void testConfigure() {
		NodeConfiguration configuration = new NodeConfiguration();
		configuration.setApplicationId(TEST_PROJ_ID);
		configurator.configure(configuration);
	}
}
