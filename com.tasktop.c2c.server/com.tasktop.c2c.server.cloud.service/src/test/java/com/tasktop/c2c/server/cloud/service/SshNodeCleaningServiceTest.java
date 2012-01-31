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
package com.tasktop.c2c.server.cloud.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.service.SshNodeCleaningService;


// Currently only a manual test as it requires ssh setup...
@Ignore
public class SshNodeCleaningServiceTest {

	private SshNodeCleaningService nodeCleaningService;

	private static final String DIR1 = "/tmp/dir1";
	private static final String DIR2 = "/tmp/dir2";
	private static final String FILENAME = "foobar.txt";

	@Before
	public void setup() throws IOException {
		nodeCleaningService = new SshNodeCleaningService();
		nodeCleaningService.setSshUser("clint.morgan");
		nodeCleaningService.setSshKeyFilePath("/Users/clint.morgan/.ssh/id_rsa");
		nodeCleaningService.setDirectoriesToClean(Arrays.asList(DIR1, DIR2));

		new File(DIR1).mkdir();
		new File(DIR2).mkdir();
		FileWriter writer = new FileWriter(DIR1 + "/" + FILENAME);
		writer.append("adfadfadfasdfasdfasdfasdfasdfasdfadfafd");
		writer.close();

		writer = new FileWriter(DIR2 + "/" + FILENAME);
		writer.append("adfadfadfasdfasdfasdfasdfasdfasdfadfafd");
		writer.close();
	}

	@Test
	public void test() throws IOException {
		ServiceHost host = new ServiceHost();
		host.setInternalNetworkAddress("127.0.01");

		Assert.assertTrue(new File(DIR1 + "/" + FILENAME).exists());
		Assert.assertTrue(new File(DIR2 + "/" + FILENAME).exists());
		nodeCleaningService.cleanNode(host);
		Assert.assertFalse(new File(DIR1 + "/" + FILENAME).exists());
		Assert.assertFalse(new File(DIR2 + "/" + FILENAME).exists());
	}
}
