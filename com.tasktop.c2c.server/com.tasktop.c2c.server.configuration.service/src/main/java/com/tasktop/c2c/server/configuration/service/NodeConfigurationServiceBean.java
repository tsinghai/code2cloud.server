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
package com.tasktop.c2c.server.configuration.service;

import java.util.HashMap;
import java.util.List;

public class NodeConfigurationServiceBean implements NodeConfigurationService {

	public interface Configurator {
		void configure(NodeConfiguration configuration);
	}

	private List<Configurator> configurators;

	@Override
	public void configureNode(NodeConfiguration configuration) {
		initializeConfiguration(configuration);

		for (String requiredProperty : REQUIRED_PROPERTIES) {
			if (!configuration.getProperties().containsKey(requiredProperty)) {
				throw new IllegalArgumentException("Missing required property: " + requiredProperty);
			}
		}

		for (Configurator configurator : configurators) {
			configurator.configure(configuration);
		}

	}

	private void initializeConfiguration(NodeConfiguration config) {
		if (config.getProperties() == null) {
			config.setProperties(new HashMap<String, String>());
		}
		config.getProperties().put(NodeConfigurationService.APPLICATION_GIT_PROPERTY,
				config.getApplicationId() + ".git");
		config.getProperties().put(NodeConfigurationService.APPLICATION_ID, config.getApplicationId());
		String servicePath = config.getProperties().get(NodeConfigurationService.PROFILE_BASE_SERVICE_URL);
		config.getProperties().put(NodeConfigurationService.APPLICATION_GIT_URL,
				servicePath + "scm/" + config.getApplicationId() + ".git");
	}

	public void setConfigurators(List<Configurator> configurators) {
		this.configurators = configurators;
	}

}
