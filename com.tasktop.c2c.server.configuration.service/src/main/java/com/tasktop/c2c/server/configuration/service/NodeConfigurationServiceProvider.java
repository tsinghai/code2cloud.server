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

import org.springframework.web.client.RestTemplate;

public class NodeConfigurationServiceProvider {
	private RestTemplate template;

	public NodeConfigurationServiceClient getNewService() {
		NodeConfigurationServiceClient service = new NodeConfigurationServiceClient();
		service.setRestTemplate(template);
		return service;
	}

	public void setRestTemplate(RestTemplate template) {
		this.template = template;
	}
}
