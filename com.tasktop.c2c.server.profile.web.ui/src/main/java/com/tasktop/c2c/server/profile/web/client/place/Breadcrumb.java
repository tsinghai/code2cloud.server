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
package com.tasktop.c2c.server.profile.web.client.place;

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.profile.domain.project.Project;

public class Breadcrumb {

	public static List<Breadcrumb> getBaseBreadcrumbs() {
		List<Breadcrumb> result = new ArrayList<Breadcrumb>();
		result.add(new Breadcrumb(ProjectsDiscoverPlace.createPlace().getHistoryToken(), "Projects"));
		return result;
	}

	public static List<Breadcrumb> getProjectSpecficBreadcrumbs(Project project) {
		List<Breadcrumb> result = getBaseBreadcrumbs();
		result.add(new Breadcrumb(ProjectHomePlace.createPlace(project.getIdentifier()).getHistoryToken(), project
				.getName()));
		return result;
	}

	private String uri;
	private String label;

	public Breadcrumb(String uri, String label) {
		this.setUri(uri);
		this.setLabel(label);
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
