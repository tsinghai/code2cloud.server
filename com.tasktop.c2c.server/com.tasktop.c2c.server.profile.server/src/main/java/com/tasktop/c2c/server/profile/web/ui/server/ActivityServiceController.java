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

import static com.tasktop.c2c.server.profile.service.ActivityServiceClient.GET_RECENT_ACTIVITY_URL;
import static com.tasktop.c2c.server.profile.service.ActivityServiceClient.GET_SHORT_ACTIVITY_LIST_URL;
import static com.tasktop.c2c.server.profile.service.ActivityServiceClient.PROJECT_IDENTIFIER_URLPARAM;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.service.ActivityService;

@Controller
public class ActivityServiceController extends AbstractRestService implements ActivityService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	@Qualifier("main")
	private ActivityService activityService;

	@RequestMapping(value = GET_RECENT_ACTIVITY_URL, method = RequestMethod.GET)
	public void getRecentActivity(HttpServletResponse response,
			@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier) throws EntityNotFoundException,
			JsonGenerationException, JsonMappingException, IOException {
		serializeActivityList(getRecentActivity(projectIdentifier), response);
	}

	@Override
	public List<ProjectActivity> getRecentActivity(String projectIdentifier) {
		return activityService.getRecentActivity(projectIdentifier);
	}

	@RequestMapping(value = GET_SHORT_ACTIVITY_LIST_URL, method = RequestMethod.GET)
	public void getShortActivityList(HttpServletResponse response,
			@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier) throws EntityNotFoundException,
			JsonGenerationException, JsonMappingException, IOException {
		serializeActivityList(getShortActivityList(projectIdentifier), response);
	}

	@Override
	public List<ProjectActivity> getShortActivityList(String projectIdentifier) {
		return activityService.getShortActivityList(projectIdentifier);
	}

	private void serializeActivityList(List<ProjectActivity> result, HttpServletResponse response) throws IOException {
		// Have to manually serialize each element of the list, Otherwise Spring+Jackson will not put in the @class
		// field needed for poly types.
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.write("{\"projectActivityList\" : [");
		boolean needSep = false;
		for (ProjectActivity item : result) {
			if (needSep) {
				writer.write(", ");
			} else {
				needSep = true;
			}
			writer.write(mapper.writeValueAsString(item));
		}
		response.getWriter().write("]}");
	}
}
