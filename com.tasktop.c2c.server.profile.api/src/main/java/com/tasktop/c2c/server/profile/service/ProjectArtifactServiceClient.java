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
package com.tasktop.c2c.server.profile.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts;

@Service
@Qualifier("webservice-client")
public class ProjectArtifactServiceClient extends AbstractRestServiceClient implements ProjectArtifactService {

	public static final String ARTIFACT_LIST_URL = "{projectIdentifier}/list";

	@SuppressWarnings("unused")
	private static class ServiceCallResult {

		private List<ProjectArtifacts> projectArtifactsList;

		public List<ProjectArtifacts> getProjectArtifactsList() {
			return projectArtifactsList;
		}

		public void setProjectArtifactsList(List<ProjectArtifacts> projectArtifactsList) {
			this.projectArtifactsList = projectArtifactsList;
		}

	}

	public List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier) {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("projectIdentifier", projectIdentifier == null ? "" : projectIdentifier);

		try {
			ServiceCallResult callResult = template.getForObject(computeUrl(ARTIFACT_LIST_URL),
					ServiceCallResult.class, variables);
			if (callResult.getProjectArtifactsList() != null) {
				return callResult.getProjectArtifactsList();
			}
		} catch (WrappedCheckedException e) {
			throw e;
		}
		throw new IllegalStateException("Illegal result from call");
	}

	public List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier, String artifactNameRegularExpression) {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("projectIdentifier", projectIdentifier == null ? "" : projectIdentifier);
		variables.put("nameRegexp", artifactNameRegularExpression);

		try {
			ServiceCallResult callResult = template.getForObject(computeUrl(ARTIFACT_LIST_URL + "?nameRegexp="
					+ artifactNameRegularExpression), ServiceCallResult.class, variables);
			if (callResult.getProjectArtifactsList() != null) {
				return callResult.getProjectArtifactsList();
			}
		} catch (WrappedCheckedException e) {
			throw e;
		}
		throw new IllegalStateException("Illegal result from call");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.ProjectArtifactService#findArtifact(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public ProjectArtifacts findBuildArtifacts(String projectId, String buildJobName, String buildJobNumber) {
		throw new UnsupportedOperationException("Not implmented yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.ProjectArtifactService#downloadProjectArtifact(java.io.File,
	 * com.tasktop.c2c.server.profile.domain.project.ProjectArtifact)
	 */
	public void downloadProjectArtifact(String projectId, File file, ProjectArtifact artifact) {
		throw new UnsupportedOperationException("Not implmented yet");
	}

}
