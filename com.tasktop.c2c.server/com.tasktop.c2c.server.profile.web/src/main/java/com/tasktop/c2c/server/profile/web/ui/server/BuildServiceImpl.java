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
package com.tasktop.c2c.server.profile.web.ui.server;

import org.springframework.beans.factory.annotation.Autowired;


import com.tasktop.c2c.server.common.web.server.AbstractAutowiredRemoteServiceServlet;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.service.HudsonServiceClient;
import com.tasktop.c2c.server.profile.service.provider.HudsonServiceProvider;
import com.tasktop.c2c.server.profile.web.ui.client.BuildService;

public class BuildServiceImpl extends AbstractAutowiredRemoteServiceServlet implements BuildService {

	@Autowired
	private HudsonServiceProvider hudsonServiceProvider;

	@Override
	public HudsonStatus getBuildStatus(String projectIdentifier) {
		HudsonServiceClient service = hudsonServiceProvider.getService(projectIdentifier);
		return service.getStatus();
	}

}
