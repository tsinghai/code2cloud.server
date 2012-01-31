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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceBean.Configurator;

/**
 * Sets up the hudson service and performs the following tasks:
 * 
 * <li>create a hudson-home directory for the projectIdentifier (done in xml config)
 * 
 * <li>create a default config.xml and put it in the hudson-home dir (done in xml config)
 * 
 * <li>customize the web.xml file with the hudson-home path and repackage a fresh with the correct name for deployment
 * (e.g., projectIdentifier#hudson.war)
 * 
 * <li>drop the customized hudson.war into the running tcserver and start the application (done in xml config)
 * 
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class HudsonServiceConfigurator implements Configurator {

	private static final Logger LOG = LoggerFactory.getLogger(HudsonServiceConfigurator.class.getName());

	private String webXmlFilename = "WEB-INF/web.xml";
	private String tempDir = FileUtils.getTempDirectoryPath();
	private String warTemplateFile;
	private String targetHudsonHomeBaseDir;
	private String targetWebappsDir;

	@Override
	public void configure(NodeConfiguration configuration) {

		// Get a reference to our template WAR, and make sure it exists.
		File hudsonTemplateWar = new File(warTemplateFile);

		if (!hudsonTemplateWar.exists() || !hudsonTemplateWar.isFile()) {
			String message = "The given Hudson template WAR [" + hudsonTemplateWar
					+ "] either did not exist or was not a file!";
			LOG.error(message);
			throw new IllegalStateException(message);
		}

		try {
			// Get a reference to our template war
			JarFile hudsonTemplateWarJar = new JarFile(hudsonTemplateWar);

			// Extract our web.xml from this war
			JarEntry webXmlEntry = hudsonTemplateWarJar.getJarEntry(webXmlFilename);
			String webXmlContents = IOUtils.toString(hudsonTemplateWarJar.getInputStream(webXmlEntry));

			// Update the web.xml to contain the correct HUDSON_HOME value
			String updatedXml = applyDirectoryToWebXml(webXmlContents, configuration);

			File tempDirFile = new File(tempDir);
			if (!tempDirFile.exists()) {
				tempDirFile.mkdirs();
			}

			// Put the web.xml back into the war
			File updatedHudsonWar = File.createTempFile("hudson", ".war", tempDirFile);

			JarOutputStream jarOutStream = new JarOutputStream(new FileOutputStream(updatedHudsonWar),
					hudsonTemplateWarJar.getManifest());

			// Loop through our existing zipfile and add in all of the entries to it except for our web.xml
			JarEntry curEntry = null;
			Enumeration<JarEntry> entries = hudsonTemplateWarJar.entries();
			while (entries.hasMoreElements()) {
				curEntry = entries.nextElement();

				// If this is the manifest, skip it.
				if (curEntry.getName().equals("META-INF/MANIFEST.MF")) {
					continue;
				}

				if (curEntry.getName().equals(webXmlEntry.getName())) {
					JarEntry newEntry = new JarEntry(curEntry.getName());
					jarOutStream.putNextEntry(newEntry);

					// Substitute our edited entry content.
					IOUtils.write(updatedXml, jarOutStream);
				} else {
					jarOutStream.putNextEntry(curEntry);
					IOUtils.copy(hudsonTemplateWarJar.getInputStream(curEntry), jarOutStream);
				}
			}

			// Clean up our resources.
			jarOutStream.close();

			String deployedUrl = configuration.getProperties().get(NodeConfigurationService.PROFILE_BASE_SERVICE_URL)
					+ "hudson/";
			deployedUrl.replace("//", "/");
			URL deployedHudsonUrl = new URL(deployedUrl);
			String webappName = deployedHudsonUrl.getPath();
			if (webappName.startsWith("/")) {
				webappName = webappName.substring(1);
			}
			if (webappName.endsWith("/")) {
				webappName = webappName.substring(0, webappName.length() - 1);
			}
			webappName = webappName.replace("/", "#");
			webappName = webappName + ".war";

			// Calculate our final filename.

			String deployLocation = targetWebappsDir + webappName;

			File hudsonDeployFile = new File(deployLocation);

			if (hudsonDeployFile.exists()) {
				String message = "When trying to deploy new WARfile [" + hudsonDeployFile.getAbsolutePath()
						+ "] a file or directory with that name already existed!";
				LOG.error(message);

				// Delete the temporary warfile, to ensure we aren't cluttering up the disk on failure
				updatedHudsonWar.delete();

				throw new IllegalStateException(message);
			}

			// Move the war into it's deployment location so that it can be picked up and deployed by Tomcat.
			FileUtils.moveFile(updatedHudsonWar, hudsonDeployFile);
		} catch (IOException ioe) {
			// Log this exception and rethrow wrapped in a RuntimeException
			LOG.error(ioe.getMessage());
			throw new RuntimeException(ioe);
		}
	}

	private String applyDirectoryToWebXml(String targetContents, NodeConfiguration configuration) {

		// Do our string processing now.
		String hudsonHomeDir = targetHudsonHomeBaseDir + "/" + configuration.getApplicationId();

		return targetContents.replace("<env-entry-value></env-entry-value>", "<env-entry-value>" + hudsonHomeDir
				+ "</env-entry-value>");
	}

	public void setWebXmlFilename(String webXmlFilename) {
		this.webXmlFilename = webXmlFilename;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	@Required
	public void setTargetHudsonHomeBaseDir(String targetHudsonHomeBaseDir) {
		this.targetHudsonHomeBaseDir = targetHudsonHomeBaseDir;
	}

	@Required
	public void setWarTemplateFile(String warTemplateFile) {
		this.warTemplateFile = warTemplateFile;
	}

	@Required
	public void setTargetWebappsDir(String targetWebappsDir) {
		this.targetWebappsDir = targetWebappsDir;
	}
}
