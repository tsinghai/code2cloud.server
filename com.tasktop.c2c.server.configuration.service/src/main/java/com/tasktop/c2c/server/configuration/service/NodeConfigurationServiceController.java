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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.web.AbstractRestService;


@Controller
public class NodeConfigurationServiceController extends AbstractRestService {

	private NodeConfigurationService nodeConfigurationService;

	public void setNodeConfigurationService(NodeConfigurationService nodeConfigurationService) {
		this.nodeConfigurationService = nodeConfigurationService;
	}

	@RequestMapping(value = "/configure", method = RequestMethod.POST)
	public void configureNode(@RequestBody NodeConfigurationService.NodeConfiguration configuration) {
		nodeConfigurationService.configureNode(configuration);
	}

}
