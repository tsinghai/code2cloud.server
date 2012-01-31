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
package com.tasktop.c2c.server.internal.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.deployment.service.DeploymentConfigurationService;
import com.tasktop.c2c.server.event.domain.BuildEvent;
import com.tasktop.c2c.server.event.domain.Event;
import com.tasktop.c2c.server.event.service.EventListener;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Component("automaticDeploymentBuildListener")
public class AutomaticDeploymentBuildListener implements EventListener {

	@Autowired
	private DeploymentConfigurationService deploymentConfigurationService;

	@Override
	public void onEvent(Event event) {
		if (!(event instanceof BuildEvent)) {
			return;
		}

		BuildEvent buildEvent = (BuildEvent) event;

		BuildResult result = buildEvent.getBuildDetails().getResult();
		if (result == null || result.equals(BuildResult.FAILURE) || result.equals(BuildResult.NOT_BUILT)) {
			return;
		}

		deploymentConfigurationService.onBuildCompleted(buildEvent.getJobName(), buildEvent.getBuildDetails());
	}

}
