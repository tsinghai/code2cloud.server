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

import java.util.List;


import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskHistoryPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TaskHistoryView;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;

public class TaskHistoryPresenter extends AbstractTaskPresenter implements SplittableActivity {

	public interface Display extends IsWidget {
		void setData(String projectId, Task task, List<TaskActivity> activity);
	}

	private Display view;

	public TaskHistoryPresenter(Display view) {
		super(view);
		this.view = view;
	}

	public TaskHistoryPresenter() {
		this(TaskHistoryView.getInstance());
	}

	@Override
	public void setPlace(Place aPlace) {
		ProjectTaskHistoryPlace place = (ProjectTaskHistoryPlace) aPlace;
		this.projectIdentifier = place.getProjectId();
		view.setData(place.getProjectId(), place.getTask(), place.getTaskActivity());
	}

	@Override
	protected void bind() {

	}

}
