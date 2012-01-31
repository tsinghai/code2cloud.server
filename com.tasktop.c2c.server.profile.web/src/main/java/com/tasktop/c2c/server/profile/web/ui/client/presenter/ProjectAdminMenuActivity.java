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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.ProjectAdminMenu;

public class ProjectAdminMenuActivity extends AbstractActivity {

	private Place currentPlace;

	public ProjectAdminMenuActivity(Place currentPlace) {
		this.currentPlace = currentPlace;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		ProjectAdminMenu.getInstance().select(currentPlace);
		panel.setWidget(ProjectAdminMenu.getInstance());
	}
}
