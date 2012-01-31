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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;

@Service
@Qualifier("webservice-client")
public class ActivityServiceClient extends AbstractRestServiceClient implements ActivityService {

	@SuppressWarnings("unused")
	private static class ServiceCallResult {

		private List<ProjectActivity> projectActivityList;

		public List<ProjectActivity> getProjectActivityList() {
			return projectActivityList;
		}

		public void setProjectActivityList(List<ProjectActivity> projectActivityList) {
			this.projectActivityList = projectActivityList;
		}

	}

	private abstract class GetCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object... variables) {
			ServiceCallResult callResult = template.getForObject(computeUrl(urlStub), ServiceCallResult.class,
					variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to activityService");
			}

			return retVal;
		}
	}

	public static final String PROJECT_IDENTIFIER_URLPARAM = "projectIdentifier";

	public static final String GET_RECENT_ACTIVITY_URL = "activity/{" + PROJECT_IDENTIFIER_URLPARAM + "}";

	public List<ProjectActivity> getRecentActivity(String projectIdentifier) {

		return new GetCall<List<ProjectActivity>>() {
			@Override
			public List<ProjectActivity> getValue(ServiceCallResult result) {
				return result.getProjectActivityList();
			}
		}.doCall(GET_RECENT_ACTIVITY_URL, projectIdentifier);
	}

	public static final String GET_SHORT_ACTIVITY_LIST_URL = "activity/{" + PROJECT_IDENTIFIER_URLPARAM + "}/short";

	public List<ProjectActivity> getShortActivityList(String projectIdentifier) {
		return new GetCall<List<ProjectActivity>>() {
			@Override
			public List<ProjectActivity> getValue(ServiceCallResult result) {
				return result.getProjectActivityList();
			}
		}.doCall(GET_SHORT_ACTIVITY_LIST_URL, projectIdentifier);
	}

	@Autowired()
	@Qualifier("basicAuth")
	public void setRestTemplate(RestTemplate template) {
		super.setRestTemplate(template);
	}

}
