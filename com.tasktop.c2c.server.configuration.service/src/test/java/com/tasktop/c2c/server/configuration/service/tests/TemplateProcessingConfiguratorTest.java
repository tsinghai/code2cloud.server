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
package com.tasktop.c2c.server.configuration.service.tests;

import java.io.File;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.configuration.service.TemplateProcessingConfigurator;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class TemplateProcessingConfiguratorTest {

	private TemplateProcessingConfigurator templateProcessingConfigurator = new TemplateProcessingConfigurator();

	private File templateDir;
	private File targetDir;

	@Before
	public void setupDirs() {
		File baseDir = TestResourceUtil.computeResourceFolder("src/test/resources/test-data",
				TemplateProcessingConfiguratorTest.class);
		templateDir = new File(baseDir, "templates");
		targetDir = new File("target", "template-target");
		templateProcessingConfigurator.setTargetBaseLocation(targetDir.getAbsolutePath());
		templateProcessingConfigurator.setTemplateBaseLocation(templateDir.getAbsolutePath());
	}

	@After
	public void removeTargetDir() throws Exception {
		FileUtils.deleteDirectory(targetDir);
	}

	@Test
	public void testBasicTemplating() {
		Assert.assertFalse(targetDir.exists());

		NodeConfiguration conf = new NodeConfiguration();
		conf.setProperties(new HashMap<String, String>());
		conf.getProperties().put("a.key", "a.value");
		conf.getProperties().put("b.key", "b.value");
		conf.getProperties().put("c.key", "c.value");
		conf.setApplicationId("testId");

		templateProcessingConfigurator.configure(conf);

		Assert.assertTrue(targetDir.exists());
		Assert.assertTrue(targetDir.isDirectory());
	}
}
