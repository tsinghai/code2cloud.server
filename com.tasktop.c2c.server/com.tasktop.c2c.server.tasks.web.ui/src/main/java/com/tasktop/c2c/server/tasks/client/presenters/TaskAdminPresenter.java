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


import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.AbstractProjectAdminTasksPlace;
import com.tasktop.c2c.server.tasks.client.widgets.admin.TaskAdminView;

public class TaskAdminPresenter extends AbstractTaskPresenter {

	private TaskAdminView view;
	private TaskAdminActivityMapper adminActivityMapper = new TaskAdminActivityMapper();
	private ActivityManager adminActivityManager = new ActivityManager(adminActivityMapper, ProfileGinjector.get
			.instance().getEventBus());

	public TaskAdminPresenter() {
		this(TaskAdminView.getInstance());
		adminActivityManager.setDisplay(view.getContentContainer());
	}

	/**
	 * @param instance
	 */
	public TaskAdminPresenter(TaskAdminView view) {
		super(view);
		this.view = view;
	}

	public void setPlace(Place aPlace) {
		AbstractProjectAdminTasksPlace place = (AbstractProjectAdminTasksPlace) aPlace;
		view.setProject(place.getProject());
		view.selectPlace(aPlace);
		adminActivityManager.onPlaceChange(new PlaceChangeEvent(aPlace));
	}

	@Override
	protected void bind() {
		//
	}

}
