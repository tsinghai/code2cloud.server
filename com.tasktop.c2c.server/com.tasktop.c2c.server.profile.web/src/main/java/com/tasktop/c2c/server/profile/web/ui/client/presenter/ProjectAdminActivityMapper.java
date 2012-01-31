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
package com.tasktop.c2c.server.profile.web.ui.client.presenter;


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminSourcePlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminTeamPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.presenter.ProjectAdminSourceActivity;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.presenter.ProjectAdminTeamActivity;
import com.tasktop.c2c.server.tasks.client.place.AbstractProjectAdminTasksPlace;
import com.tasktop.c2c.server.tasks.client.presenters.TaskAdminPresenter;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class ProjectAdminActivityMapper implements ActivityMapper {

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof ProjectAdminSettingsPlace) {
			ProjectAdminSettingsActivity activity = new ProjectAdminSettingsActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminTeamPlace) {
			ProjectAdminTeamActivity activity = new ProjectAdminTeamActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminSourcePlace) {
			ProjectAdminSourceActivity activity = new ProjectAdminSourceActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof AbstractProjectAdminTasksPlace) {
			TaskAdminPresenter activity = new TaskAdminPresenter();
			activity.setPlace(place);
			return activity;
		}
		return new ProjectAdminSettingsActivity();
	}

}
