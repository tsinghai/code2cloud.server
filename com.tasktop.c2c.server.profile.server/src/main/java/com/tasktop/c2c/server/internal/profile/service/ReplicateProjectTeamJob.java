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
package com.tasktop.c2c.server.internal.profile.service;

import org.springframework.context.ApplicationContext;

import com.tasktop.c2c.server.auth.service.job.SystemJob;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.service.ProfileService;


public class ReplicateProjectTeamJob extends SystemJob {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	private final String projectIdentifier;

	public ReplicateProjectTeamJob(Project project) {
		this.projectId = project.getId();
		projectIdentifier = project.getIdentifier();
		setType(Type.SHORT);
	}

	public Long getProjectId() {
		return projectId;
	}

	@Override
	public void execute(final ApplicationContext applicationContext) {
		executeAsSystem(applicationContext, projectIdentifier, new Runnable() {
			@Override
			public void run() {
				ProfileService service = applicationContext.getBean("profileService", ProfileService.class);
				try {
					service.replicateTeam(projectId);
				} catch (EntityNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}

}
