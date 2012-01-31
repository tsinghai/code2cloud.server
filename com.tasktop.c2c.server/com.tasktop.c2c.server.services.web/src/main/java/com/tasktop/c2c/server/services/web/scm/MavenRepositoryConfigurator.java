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
package com.tasktop.c2c.server.services.web.scm;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceBean.Configurator;

public class MavenRepositoryConfigurator implements Configurator {

	private static final Logger LOG = LoggerFactory.getLogger(MavenRepositoryConfigurator.class.getName());

	private String mavenRoot;

	@Override
	public void configure(NodeConfiguration configuration) {

		String projectName = configuration.getApplicationId();
		if (projectName == null) {
			LOG.warn("No Project ID specified!");
			return;
		}

		File mavenRepoRootFile = new File(mavenRoot, projectName);

		if (mavenRepoRootFile.exists()) {
			LOG.warn("Maven repository already apears to exist: " + mavenRepoRootFile.getAbsolutePath());
		} else {
			// If we're here, the destination directory doesn't exist. Create it now.
			LOG.info("Creating new Maven repository: " + mavenRepoRootFile.getAbsolutePath());
			mavenRepoRootFile.mkdirs();
		}
	}

	@Required
	public void setMavenRoot(String gitRoot) {
		this.mavenRoot = gitRoot;
	}
}
