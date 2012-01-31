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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.presenters.TaskHistoryPresenter;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;

public class TaskHistoryView extends AbstractComposite implements TaskHistoryPresenter.Display {

	private static TaskHistoryView instance = null;

	public static TaskHistoryView getInstance() {
		if (instance == null) {
			instance = new TaskHistoryView();
		}
		return instance;
	}

	interface Binder extends UiBinder<Widget, TaskHistoryView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label taskLabel;
	@UiField
	Anchor taskLink;
	@UiField
	Panel activityPanel;

	private TaskHistoryView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setData(String projectId, Task task, List<TaskActivity> activities) {
		activityPanel.clear();
		for (TaskActivity activity : activities) {
			activityPanel.add(new TaskActivityView(activity));
		}
		taskLabel.setText(task.getTaskType() + " " + task.getId() + ": " + task.getShortDescription());
		taskLink.setHref(ProjectTaskPlace.createPlace(projectId, task.getId()).getHref());
	}
}
