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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tasktop.c2c.server.common.service.web.HasBaseUrl;
import com.tasktop.c2c.server.profile.domain.build.BuildArtifact;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts.Type;
import com.tasktop.c2c.server.profile.service.HudsonService;
import com.tasktop.c2c.server.profile.service.ProjectArtifactService;
import com.tasktop.c2c.server.profile.service.provider.ServiceProvider;

@Service("projectArtifactService")
@Qualifier("main")
public class ProjectArtifactServiceImpl implements ProjectArtifactService {

	@Resource(name = "hudsonServiceProvider")
	private ServiceProvider<HudsonService> hudsonServiceProvider;

	@Autowired
	private ProfileServiceConfiguration profileServiceConfiguration;

	public void setHudsonServiceProvider(ServiceProvider<HudsonService> hudsonServiceProvider) {
		this.hudsonServiceProvider = hudsonServiceProvider;
	}

	@Override
	public List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier) {
		return listProjectArtifacts(projectIdentifier, null);
	}

	@Override
	public List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier, String artifactNameRegularExpression) {
		List<ProjectArtifacts> artifacts = computeReleasesFromHudsonBuilds(projectIdentifier);
		// TODO merge with releases from datastore
		// TODO sorting
		filterArtifacts(artifacts, artifactNameRegularExpression);
		return artifacts;
	}

	private void filterArtifacts(List<ProjectArtifacts> artifacts, String artifactNameRegularExpression) {
		if (artifactNameRegularExpression == null) {
			return;
		}
		Pattern p = Pattern.compile(artifactNameRegularExpression);
		Iterator<ProjectArtifacts> artifactsIt = artifacts.iterator();
		while (artifactsIt.hasNext()) {
			ProjectArtifacts projectArtifacts = artifactsIt.next();
			Iterator<ProjectArtifact> artifactIt = projectArtifacts.getArtifacts().iterator();
			while (artifactIt.hasNext()) {
				ProjectArtifact artifact = artifactIt.next();
				if (!p.matcher(artifact.getName()).matches()) {
					artifactIt.remove();
				}
			}
			if (projectArtifacts.getArtifacts().isEmpty()) {
				artifactsIt.remove();
			}
		}

	}

	private List<ProjectArtifacts> computeReleasesFromHudsonBuilds(String projectIdentifier) {
		HudsonService hudonService = hudsonServiceProvider.getService(projectIdentifier);
		List<ProjectArtifacts> result = new ArrayList<ProjectArtifacts>();

		for (Entry<JobSummary, List<BuildDetails>> entry : hudonService.getBuildHistory().entrySet()) {
			for (BuildDetails buildDetails : entry.getValue()) {
				if (buildDetails.getArtifacts() == null || buildDetails.getArtifacts().isEmpty()) {
					continue;
				}
				result.add(constructProjectArtifacts(entry.getKey(), buildDetails));
			}
		}

		return result;
	}

	private ProjectArtifacts constructProjectArtifacts(JobSummary jobSummary, BuildDetails buildDetails) {
		ProjectArtifacts projectArtifacts = new ProjectArtifacts();
		projectArtifacts.setName(jobSummary.getName() + " Build " + buildDetails.getNumber());
		projectArtifacts.setType(Type.TRANSIENT);
		projectArtifacts.setCategory("Unreleased");
		projectArtifacts.setCreationDate(new Date(buildDetails.getTimestamp()));
		projectArtifacts.setDescription("Created from hudson build");
		projectArtifacts.setBuildJobName(jobSummary.getName());
		projectArtifacts.setBuildJobNumber(buildDetails.getNumber() + "");
		projectArtifacts.setBuildIdentifier(ProjectArtifacts.computeBuildIdentifier(jobSummary.getName(),
				buildDetails.getNumber()));
		// we leave version unspecified, since they could be building from
		// multiple Git repos. (or SVN)

		projectArtifacts.setArtifacts(new ArrayList<ProjectArtifact>(buildDetails.getArtifacts().size()));
		for (BuildArtifact buildArtifact : buildDetails.getArtifacts()) {
			ProjectArtifact artifact = new ProjectArtifact();
			artifact.setName(buildArtifact.getFileName());
			artifact.setPath(buildArtifact.getRelativePath());
			artifact.setUrl(jobSummary.getUrl() + buildDetails.getNumber() + "/artifact/"
					+ buildArtifact.getRelativePath());
			projectArtifacts.getArtifacts().add(artifact);
		}

		return projectArtifacts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.ProjectArtifactService#findArtifact(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ProjectArtifacts findBuildArtifacts(String projectIdentifer, String buildJobName, String buildJobNumber) {
		HudsonService hudsonService = hudsonServiceProvider.getService(projectIdentifer);

		try {
			HudsonStatus status = hudsonService.getStatus();
			JobSummary jobSummary = null;
			for (JobSummary js : status.getJobs()) {
				if (js.getName().equals(buildJobName)) {
					jobSummary = js;
				}
			}
			if (jobSummary == null) {
				return null;
			}
			BuildDetails buildDetails = hudsonService.getBuildDetails(buildJobName, Integer.parseInt(buildJobNumber));
			return constructProjectArtifacts(jobSummary, buildDetails);

		} catch (Exception e) {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.ProjectArtifactService#downloadProjectArtifact(java.io.File,
	 * com.tasktop.c2c.server.profile.domain.project.ProjectArtifact)
	 */
	@Override
	public void downloadProjectArtifact(String projectId, File file, ProjectArtifact artifact) throws IOException {
		try {
			HudsonService hudsonService = hudsonServiceProvider.getService(projectId);

			// Maybe Rewrite the url so that it goes to the internal node
			String url = artifact.getUrl();
			boolean shouldReriteToLocal = true; // url.startsWith(profileServiceConfiguration.getProfileBaseURL())
			if (shouldReriteToLocal) {
				// url : https://q.tasktop.com/alm/s/code2cloud/hudson/jobs/Server/1/artifacts/target/profile.war
				// profileBaseUrl : https://q.tasktop.com/alm/
				// serviceBaseUrl : http://10.0.0.1:8080/alm/s/code2cloud/hudson
				URL serviceBaseUrl = new URL(((HasBaseUrl) hudsonService).getBaseUrl());
				URL artifactUrl = new URL(url);
				url = serviceBaseUrl.getProtocol() + "://" + serviceBaseUrl.getHost() + ":" + serviceBaseUrl.getPort()
						+ artifactUrl.getPath();
			}

			hudsonService.downloadBuildArtifact(url, file);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	// @Override
	// public Long createProjectArtifacts(ProjectArtifacts release) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void updateProjectArtifacts(ProjectArtifacts release) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void removeProjectArtifacts(ProjectArtifacts release) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void retrieveProjectArtifacts(Long artifactsId) {
	// // TODO Auto-generated method stub
	//
	// }

}
