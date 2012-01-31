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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TasksSummaryView;
import com.tasktop.c2c.server.tasks.domain.Task;

public class TasksSummaryPresenter extends AbstractTaskPresenter implements SplittableActivity {

	private TasksSummaryView tasksView;
	private Integer componentId;
	private String milestone;
	private List<Task> tasks;

	public TasksSummaryPresenter(TasksSummaryView view) {
		super(view);
		tasksView = view;
	}

	public TasksSummaryPresenter() {
		this(new TasksSummaryView());
	}

	public void setPlace(Place p) {
		ProjectTasksSummaryPlace place = (ProjectTasksSummaryPlace) p;
		this.componentId = place.getComponentId();
		this.milestone = place.getMilestone();
		this.tasks = place.getTasks();
		tasksView.setAppId(place.getProjectId());
		tasksView.setProductId(place.getProductId());
		renderTaskList();
	}

	public boolean isSearchByMilestone() {
		return (milestone != null);
	}

	public boolean isSearchByComponent() {
		return (componentId != null);
	}

	@Override
	protected void bind() {

	}

	private void renderTaskList() {
		tasksView.clear();
		if (tasks.isEmpty()) {
			this.tasksView.setHeader("No tasks found");

			// We're done, bail out.
			return;
		}

		Task sampleTask = tasks.get(0);

		if (isSearchByMilestone() && isSearchByComponent()) {

			// If we're here, both a component and a milestone were specified.
			this.tasksView.setHeader("Product: " + sampleTask.getProduct().getName() + ", Component: "
					+ sampleTask.getComponent().getName() + ", Release: " + sampleTask.getMilestone().getValue());

			this.tasksView.renderTaskSummaryForList(tasks);

		} else if (isSearchByComponent()) {

			this.tasksView.setHeader("Product: " + sampleTask.getProduct().getName() + ", Component: "
					+ sampleTask.getComponent().getName());

			HashMap<String, List<Task>> taskMap = new HashMap<String, List<Task>>();

			for (Task task : tasks) {
				if (task.getMilestone() == null) {
					// Bad data; skip it so that our rendering doesn't break.
					continue;
				}

				safeAdd(taskMap, task.getMilestone().getValue(), task);
			}

			this.tasksView.renderTaskSummaryForMap(taskMap);

		} else {

			// This means we're reporting on a milestone (since we had to search by either component or milestone)
			this.tasksView.setHeader("Product: " + sampleTask.getProduct().getName() + ", Release: "
					+ sampleTask.getMilestone().getValue());

			HashMap<String, List<Task>> taskMap = new HashMap<String, List<Task>>();

			for (Task task : tasks) {
				safeAdd(taskMap, task.getComponent().getName(), task);
			}

			this.tasksView.renderTaskSummaryForMap(taskMap);
		}
	}

	private void safeAdd(HashMap<String, List<Task>> map, String key, Task value) {
		// Check to see if there's already a list under this key - if not, make one now.
		if (!map.containsKey(key)) {
			map.put(key, new ArrayList<Task>());
		}

		// Append our new task now.
		map.get(key).add(value);
	}
}
