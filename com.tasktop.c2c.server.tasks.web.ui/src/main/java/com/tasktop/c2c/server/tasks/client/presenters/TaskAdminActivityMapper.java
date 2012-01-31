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
package com.tasktop.c2c.server.tasks.client.presenters;


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminCustomFieldsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminIterationsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminKeywordsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminProductsPlace;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskAdminActivityMapper implements ActivityMapper {

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof ProjectAdminProductsPlace) {
			ProjectAdminTaskProductsActivity activity = new ProjectAdminTaskProductsActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminKeywordsPlace) {
			ProjectAdminTaskKeywordsActivity activity = new ProjectAdminTaskKeywordsActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminIterationsPlace) {
			ProjectAdminTaskIterationsActivity activity = new ProjectAdminTaskIterationsActivity();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminCustomFieldsPlace) {
			ProjectAdminTaskCustomFieldsActivity activity = new ProjectAdminTaskCustomFieldsActivity();
			activity.setPlace(place);
			return activity;
		}
		return null;
	}
}
