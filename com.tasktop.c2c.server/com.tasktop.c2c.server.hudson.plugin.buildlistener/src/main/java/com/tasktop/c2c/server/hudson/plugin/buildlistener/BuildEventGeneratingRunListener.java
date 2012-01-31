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
package com.tasktop.c2c.server.hudson.plugin.buildlistener;

import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import hudson.model.listeners.RunListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import com.tasktop.c2c.server.event.domain.BuildEvent;
import com.tasktop.c2c.server.event.service.EventService;
import com.tasktop.c2c.server.event.service.EventServiceClient;
import com.tasktop.c2c.server.profile.domain.build.BuildArtifact;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class BuildEventGeneratingRunListener extends RunListener<Run> {

	private EventService eventService;
	private Configuration config;

	void setConfiguration(Configuration config) {
		this.config = config;
	}

	@Override
	public void onCompleted(Run run, TaskListener taskListener) {
		BuildEvent buildEvent = new BuildEvent();
		buildEvent.setProjectId(config.getProjectIdentifier());
		buildEvent.setTimestamp(new Date());
		buildEvent.setJobName(run.getParent().getName());
		BuildDetails buildDetails = new BuildDetails();
		buildEvent.setBuildDetails(buildDetails);

		buildDetails.setBuilding(false);
		buildDetails.setResult(BuildResult.valueOf(run.getResult().toString()));
		buildDetails.setDuration(run.getDuration());
		buildDetails.setNumber(run.getNumber());
		buildDetails.setTimestamp(run.getTimeInMillis());
		buildDetails.setUrl(Util.encode(Hudson.getInstance().getRootUrl() + run.getUrl()));
		List<BuildArtifact> artifacts = new ArrayList<BuildArtifact>(run.getArtifacts().size());

		for (Object runArtifactObj : run.getArtifacts()) {
			Artifact runArtifact = (Artifact) runArtifactObj;
			BuildArtifact artifact = new BuildArtifact();
			artifact.setFileName(runArtifact.getFileName());
			artifact.setRelativePath(runArtifact.getHref());
			artifact.setUrl(buildDetails.getUrl() + "/artifact/" + artifact.getRelativePath());
			artifacts.add(artifact);
		}
		buildDetails.setArtifacts(artifacts);
		getEventService().publishEvent(buildEvent);
	}

	private synchronized EventService getEventService() {

		if (eventService == null) {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "classpath:applicationContext-eventServiceClient.xml" }, false) {
				@Override
				protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
					super.initBeanDefinitionReader(reader);
					reader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(PluginImpl.class
							.getClassLoader()));
				}
			};
			context.setClassLoader(PluginImpl.class.getClassLoader());
			context.refresh();

			EventServiceClient eventServiceClient = (EventServiceClient) context.getBean("eventServiceClient");

			eventServiceClient.setBaseUrl(config.getBaseEventUrl());
			eventServiceClient.setProjectId(config.getProjectIdentifier());
			this.eventService = eventServiceClient;
		}
		return eventService;
	}
}
