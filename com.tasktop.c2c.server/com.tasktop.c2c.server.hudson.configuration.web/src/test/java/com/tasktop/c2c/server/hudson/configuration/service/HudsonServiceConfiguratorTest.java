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
package com.tasktop.c2c.server.hudson.configuration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;
import com.tasktop.c2c.server.hudson.configuration.service.HudsonServiceConfigurator;

public class HudsonServiceConfiguratorTest {

	private HudsonServiceConfigurator configurator = null;

	@Before
	public void beforeTest() {
		configurator = new HudsonServiceConfigurator();
	}

	@Test
	public void testUpdateZipfile() throws Exception {

		// First, set up our zipfile test.

		// Create a manifest with a random header so that we can ensure it's preserved by the configurator.
		Manifest mf = new Manifest();
		Attributes.Name mfTestHeader = Attributes.Name.IMPLEMENTATION_VENDOR;
		String mfTestValue = "someCrazyValue";
		mf.getMainAttributes().put(mfTestHeader, mfTestValue);

		// Why do you need to add this header? See http://tech.puredanger.com/2008/01/07/jar-manifest-api/ - if you
		// don't add it, your manifest will be blank.
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		assertEquals(mfTestValue, mf.getMainAttributes().getValue(mfTestHeader));

		File testWar = File.createTempFile("HudsonServiceConfiguratorTest", ".war");
		configurator.setWarTemplateFile(testWar.getAbsolutePath());

		try {
			JarOutputStream jarOutStream = new JarOutputStream(new FileOutputStream(testWar), mf);

			String specialFileName = "foo/bar.xml";
			String fileContents = "This is a file within our JAR file - it contains an <env-entry-value></env-entry-value> which should be filled by the configurator only if this is the 'special' file.";

			// Make our configurator replace this file within the JAR.
			configurator.setWebXmlFilename(specialFileName);

			// Create several random files in the zip.
			for (int i = 0; i < 10; i++) {
				JarEntry curEntry = new JarEntry("folder/file" + i);
				jarOutStream.putNextEntry(curEntry);
				IOUtils.write(fileContents, jarOutStream);
			}

			// Push in our special file now.
			JarEntry specialEntry = new JarEntry(specialFileName);
			jarOutStream.putNextEntry(specialEntry);
			IOUtils.write(fileContents, jarOutStream);

			// Close the output stream now, time to do the test.
			jarOutStream.close();

			// Configure this configurator with appropriate folders for testing.
			String expectedHomeDir = "/some/silly/random/homeDir";
			String webappsTarget = FileUtils.getTempDirectoryPath() + "/webapps/";
			File webappsTargetDir = new File(webappsTarget);
			if (webappsTargetDir.exists()) {
				FileUtils.deleteDirectory(webappsTargetDir);
			}
			String expectedDestFile = webappsTarget + "s#test#hudson.war";
			configurator.setTargetHudsonHomeBaseDir(expectedHomeDir);
			configurator.setTargetWebappsDir(webappsTarget);

			NodeConfiguration config = new NodeConfiguration();
			config.setApplicationId("test123");
			config.setProperties(Collections.singletonMap(NodeConfigurationService.PROFILE_BASE_SERVICE_URL,
					"https://qcode.cloudfoundry.com/s/test/"));
			// Now, run it against our test setup
			configurator.configure(config);

			// Confirm that the zipfile was updates as expected.
			File configuredWar = new File(expectedDestFile);
			assertTrue(configuredWar.exists());

			try {
				JarFile configuredWarJar = new JarFile(configuredWar);
				Manifest extractedMF = configuredWarJar.getManifest();
				assertEquals(mfTestValue, extractedMF.getMainAttributes().getValue(mfTestHeader));

				// Make sure all of our entries are present, and contain the data we expected.
				JarEntry curEntry = null;
				Enumeration<JarEntry> entries = configuredWarJar.entries();
				while (entries.hasMoreElements()) {
					curEntry = entries.nextElement();

					// If this is the manifest, skip it.
					if (curEntry.getName().equals("META-INF/MANIFEST.MF")) {
						continue;
					}

					String entryContents = IOUtils.toString(configuredWarJar.getInputStream(curEntry));

					if (curEntry.getName().equals(specialFileName)) {
						assertFalse(fileContents.equals(entryContents));
						assertTrue(entryContents.contains(expectedHomeDir));
					} else {
						// Make sure our content was unchanged.
						assertEquals(fileContents, entryContents);
						assertFalse(entryContents.contains(expectedHomeDir));
					}
				}
			} finally {
				// Clean up our test file.
				configuredWar.delete();
			}
		} finally {
			// Clean up our test file.
			testWar.delete();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateZipfile_warAlreadyExists() throws Exception {

		// First, set up our zipfile test.
		File testWar = File.createTempFile("HudsonServiceConfiguratorTest", ".war");
		configurator.setWarTemplateFile(testWar.getAbsolutePath());

		try {
			JarOutputStream jarOutStream = new JarOutputStream(new FileOutputStream(testWar), new Manifest());

			String specialFileName = "foo/bar.xml";
			String fileContents = "This is a file within our JAR file - it contains an <env-entry-value></env-entry-value> which should be filled by the configurator only if this is the 'special' file.";

			// Make our configurator replace this file within the JAR.
			configurator.setWebXmlFilename(specialFileName);

			// Create several random files in the zip.
			for (int i = 0; i < 10; i++) {
				JarEntry curEntry = new JarEntry("folder/file" + i);
				jarOutStream.putNextEntry(curEntry);
				IOUtils.write(fileContents, jarOutStream);
			}

			// Push in our special file now.
			JarEntry specialEntry = new JarEntry(specialFileName);
			jarOutStream.putNextEntry(specialEntry);
			IOUtils.write(fileContents, jarOutStream);

			// Close the output stream now, time to do the test.
			jarOutStream.close();

			// Configure this configurator with appropriate folders for testing.
			String webappsTarget = FileUtils.getTempDirectoryPath() + "/webapps/";
			String expectedDestFile = webappsTarget + "s#test123#hudson.war";
			configurator.setTargetWebappsDir(webappsTarget);
			configurator.setTargetHudsonHomeBaseDir("/some/silly/random/homeDir");

			NodeConfiguration config = new NodeConfiguration();
			config.setApplicationId("test123");
			config.setProperties(Collections.singletonMap(NodeConfigurationService.PROFILE_BASE_SERVICE_URL,
					"https://qcode.cloudfoundry.com/s/test123/"));

			// Now, run it against our test setup
			configurator.configure(config);

			// Confirm that the zipfile was updates as expected.
			File configuredWar = new File(expectedDestFile);
			assertTrue(configuredWar.exists());

			try {
				// Now, try and create it a second time - this should blow up, since the WAR already exists
				configurator.configure(config);
			} finally {
				// Clean up our test file.
				configuredWar.delete();
			}
		} finally {
			// Clean up our test file.
			testWar.delete();
		}
	}
}
