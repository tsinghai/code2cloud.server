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
package com.tasktop.c2c.server.profile.tests.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.profile.domain.build.BuildArtifact;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts;
import com.tasktop.c2c.server.profile.service.HudsonService;
import com.tasktop.c2c.server.profile.service.ProjectArtifactService;
import com.tasktop.c2c.server.profile.service.ProjectArtifactServiceImpl;
import com.tasktop.c2c.server.profile.service.provider.ServiceProvider;

public class ProjectArtifactServiceTest {

	private Mockery context;
	protected ProjectArtifactService projectArtifactService;
	private Map<JobSummary, List<BuildDetails>> mockBuildHistory = new HashMap<JobSummary, List<BuildDetails>>();

	@Before
	public void setup() {

		context = new JUnit4Mockery();
		final ServiceProvider hudsonServiceProvider = context.mock(ServiceProvider.class);
		final HudsonService hudsonService = context.mock(HudsonService.class);
		context.checking(new Expectations() {
			{
				allowing(hudsonServiceProvider).getService(with(any(String.class)));
				will(returnValue(hudsonService));

				allowing(hudsonService).getBuildHistory();
				will(returnValue(mockBuildHistory));
			}
		});

		projectArtifactService = new ProjectArtifactServiceImpl();
		((ProjectArtifactServiceImpl) projectArtifactService).setHudsonServiceProvider(hudsonServiceProvider);

	}

	@Test
	public void testFromHudsonBuilds() {
		JobSummary jobWithNoBuilds = new JobSummary();
		jobWithNoBuilds.setName("jobWithNoBuilds");
		jobWithNoBuilds.setUrl("url");
		mockBuildHistory.put(jobWithNoBuilds, Collections.EMPTY_LIST);

		List<ProjectArtifacts> releases = projectArtifactService.listProjectArtifacts("projId");
		Assert.assertEquals(0, releases.size());

		JobSummary jobWithNoArtifacts = new JobSummary();
		jobWithNoArtifacts.setName("jobWithNoArtifacts");
		jobWithNoArtifacts.setUrl("url");
		BuildDetails build1 = new BuildDetails();
		build1.setTimestamp(System.currentTimeMillis());
		mockBuildHistory.put(jobWithNoArtifacts, Collections.singletonList(build1));

		releases = projectArtifactService.listProjectArtifacts("projId");
		Assert.assertEquals(0, releases.size());
		releases = projectArtifactService.listProjectArtifacts("projId", ".*\\.jar");
		Assert.assertEquals(0, releases.size());

		JobSummary jobWithArtifacts = new JobSummary();
		jobWithArtifacts.setName("jobWithArtifacts");
		jobWithArtifacts.setUrl("url");
		build1 = new BuildDetails();
		build1.setTimestamp(System.currentTimeMillis());
		build1.setNumber(1);
		BuildArtifact artifact1 = new BuildArtifact();
		artifact1.setFileName("filename.txt");
		artifact1.setRelativePath("path/filename.txt");
		BuildArtifact artifact2 = new BuildArtifact();
		artifact2.setFileName("filename.jar");
		artifact2.setRelativePath("path/filename.jar");
		build1.setArtifacts(Arrays.asList(artifact1, artifact2));
		mockBuildHistory.put(jobWithArtifacts, Collections.singletonList(build1));

		JobSummary job2WithArtifacts = new JobSummary();
		job2WithArtifacts.setName("job2WithArtifacts");
		job2WithArtifacts.setUrl("url");
		build1 = new BuildDetails();
		build1.setTimestamp(System.currentTimeMillis());
		build1.setNumber(1);
		artifact1 = new BuildArtifact();
		artifact1.setFileName("filename.war");
		artifact1.setRelativePath("path/filename.war");
		artifact2 = new BuildArtifact();
		artifact2.setFileName("filename.jar");
		artifact2.setRelativePath("path/filename.jar");
		build1.setArtifacts(Arrays.asList(artifact1, artifact2));
		mockBuildHistory.put(job2WithArtifacts, Collections.singletonList(build1));

		releases = projectArtifactService.listProjectArtifacts("projId");
		Assert.assertEquals(2, releases.size());
		ProjectArtifacts artifacts = releases.get(0);
		Assert.assertEquals(ProjectArtifacts.Type.TRANSIENT, artifacts.getType());

		Assert.assertEquals(2, artifacts.getArtifacts().size());
		ProjectArtifact projectArtifact = artifacts.getArtifacts().get(0);
		Assert.assertNotNull(projectArtifact.getUrl());

		releases = projectArtifactService.listProjectArtifacts("projId", ".*\\.jar");
		Assert.assertEquals(2, releases.size());
		Assert.assertEquals(1, releases.get(0).getArtifacts().size());
		Assert.assertEquals(1, releases.get(1).getArtifacts().size());
	}
}
