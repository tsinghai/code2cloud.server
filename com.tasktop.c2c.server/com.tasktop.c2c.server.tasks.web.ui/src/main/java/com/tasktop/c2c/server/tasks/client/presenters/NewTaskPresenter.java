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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.History;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.tasks.client.place.ProjectNewTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.widgets.NewTaskDisplay;
import com.tasktop.c2c.server.tasks.client.widgets.NewTaskView;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.shared.action.CreateTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.CreateTaskResult;

public class NewTaskPresenter extends AbstractEditTaskPresenter<NewTaskDisplay> implements SplittableActivity {

	private static class LastValues {
		private final Product product;
		private final Component component;
		private final Milestone milestone;
		private final TaskSeverity severity;
		private final Iteration iteration;
		private final String type;

		public LastValues(Task task) {
			product = task.getProduct();
			component = task.getComponent();
			milestone = task.getMilestone();
			severity = task.getSeverity();
			iteration = task.getIteration();
			type = task.getTaskType();
		}

		public void applyTo(Task newTask) {
			newTask.setProduct(product);
			newTask.setComponent(component);
			newTask.setMilestone(milestone);
			newTask.setSeverity(severity);
			newTask.setIteration(iteration);
			newTask.setTaskType(type);
		}
	}

	private Task parentTask = null;
	private Task subTask = null;
	private Map<String, LastValues> projectIdToLastValues = new HashMap<String, NewTaskPresenter.LastValues>();

	public NewTaskPresenter(NewTaskDisplay view) {
		super(view);
	}

	public NewTaskPresenter() {
		this(new NewTaskView());
	}

	@Override
	protected void fetchTask() {
		task = new Task();

		task.setStatus(repositoryConfiguration.getDefaultStatus());
		task.setSeverity(repositoryConfiguration.getDefaultSeverity());
		task.setPriority(repositoryConfiguration.getDefaultPriority());
		task.setProduct(repositoryConfiguration.getDefaultProduct());
		if (repositoryConfiguration.getDefaultProduct() != null) {
			task.setMilestone(repositoryConfiguration.getDefaultProduct().getDefaultMilestone());
			task.setComponent(repositoryConfiguration.getDefaultProduct().getDefaultComponent());
		}
		task.setTaskType(repositoryConfiguration.getDefaultType());
		task.setIteration(repositoryConfiguration.getDefaultIteration());
		task.setCustomFields(new HashMap<String, String>());

		if (parentTask != null) {
			setDefaults(task, parentTask);
			task.setParentTask(parentTask);
		} else if (subTask != null) {
			setDefaults(task, subTask);
			task.setSubTasks(Collections.singletonList(subTask));
		} else {
			LastValues lastValues = projectIdToLastValues.get(projectIdentifier);
			if (lastValues != null) {
				lastValues.applyTo(task);
			}
		}

		if (task.getAssignee() == null) {
			task.setAssignee(task.getComponent().getInitialOwner());
		}
		populateUi(task);
		editTaskView.setValue(task);

	}

	private static void setDefaults(Task target, Task source) {
		target.setTaskType(source.getTaskType());
		target.setSeverity(source.getSeverity());
		target.setPriority(source.getPriority());
		target.setAssignee(source.getAssignee());
		target.setProduct(source.getProduct());
		target.setComponent(source.getComponent());
		target.setMilestone(source.getMilestone());
		target.setIteration(source.getIteration());
	}

	@Override
	protected void doCancel() {
		History.back();
	}

	@Override
	protected void doSaveTask() {
		populateModel();
		projectIdToLastValues.put(projectIdentifier, new LastValues(task));
		CreateTaskAction action = new CreateTaskAction(projectIdentifier, task);
		getDispatchService().execute(action,
				new AsyncCallbackSupport<CreateTaskResult>(new OperationMessage("Creating Task...")) {
					@Override
					protected void success(CreateTaskResult actionResult) {
						Task result = actionResult.get();
						ProjectTaskPlace place = ProjectTaskPlace.createPlaceWithTask(projectIdentifier, result);
						place.displayOnArrival(Message.createSuccessMessage("Task Created"));
						place.go();
					}
				});
	}

	/**
	 * @param place
	 */
	public void setPlace(Place place) {
		projectIdentifier = ((ProjectNewTaskPlace) place).getProjectId();
		repositoryConfiguration = ((ProjectNewTaskPlace) place).getRepositoryConfiguration();
		parentTask = ((ProjectNewTaskPlace) place).getParent();
	}

}
