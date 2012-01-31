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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TaskView;
import com.tasktop.c2c.server.tasks.client.widgets.TaskViewImpl;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.WorkLog;
import com.tasktop.c2c.server.tasks.shared.action.UpdateTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateTaskResult;

public class TaskPresenterImpl extends AbstractTaskPresenter implements TaskPresenter, SplittableActivity {
	private TaskView taskView;
	private String projectIdentifier;
	private Task task;
	private RepositoryConfiguration repositoryConfiguration;

	public TaskPresenterImpl(TaskView view, ProjectTaskPlace place) {
		super(view);
		this.taskView = view;
		this.setPlace(place);
	}

	public TaskPresenterImpl(TaskView view) {
		super(view);
		this.taskView = view;
	}

	public TaskPresenterImpl() {
		this(TaskViewImpl.getInstance());
	}

	@Override
	public void setPlace(Place aPlace) {
		ProjectTaskPlace place = (ProjectTaskPlace) aPlace;
		this.projectIdentifier = place.getProjectId();
		this.repositoryConfiguration = place.getRepositoryConfiguration();

		taskView.setRepositoryConfiguration(place.getRepositoryConfiguration());
		taskView.setPersonServices(new ProjectPersonService(projectIdentifier), getAppState().getSelf());
		taskView.setProjectIdentifier(projectIdentifier);

		if (place.getTask() != null) {
			setTask(place.getTask());
			populateUi(place.getTask());
		}

	}

	@Override
	protected void bind() {
		taskView.setPresenter(this);
	}

	@Override
	public void postComment(final String comment) {
		if (comment != null && comment.length() > 0) {
			task.addComment(comment);
			UpdateTaskAction action = new UpdateTaskAction(projectIdentifier, task);
			getDispatchService().execute(action,
					new AsyncCallbackSupport<UpdateTaskResult>(new OperationMessage("Posting Comment...")) {
						@Override
						protected void success(UpdateTaskResult actionResult) {
							Task result = actionResult.get();
							setTask(result);

							if (!actionResult.isUpdatedAlready()) {
								taskView.updateCommentView(result);
								getNotifier().displayMessage(Message.createSuccessMessage("Comment Saved"));
							} else {
								taskView.setTask(result);
								taskView.setCommentText(comment);
								getNotifier()
										.displayMessage(
												Message.createErrorMessage("Task has been updated. Review changes and post again."));
							}
						}

					});
		}

	}

	@Override
	public void saveStatus(TaskStatus status, TaskResolution resolution, Integer duplicateId) {
		task.setStatus(status);
		if (task.getStatus().isOpen()) {
			task.setResolution(null);
			task.setDuplicateOf(null);
		} else {
			task.setResolution(resolution);
			if (task.getResolution() != null && task.getResolution().isDuplicate() && duplicateId != null) {
				task.setDuplicateOf(new Task());
				task.getDuplicateOf().setId(duplicateId);
			} else {
				task.setDuplicateOf(null);
			}
		}
		updateTask();
	}

	@Override
	public void saveOwner(TaskUserProfile owner) {
		task.setAssignee(owner);
		updateTask();
	}

	@Override
	public void savePriority(Priority priority) {
		task.setPriority(priority);
		updateTask();
	}

	@Override
	public void saveSeverity(TaskSeverity severity) {
		task.setSeverity(severity);
		updateTask();
	}

	@Override
	public void saveEstimate(BigDecimal estimate) {
		task.setEstimatedTime(estimate);
		updateTask();
	}

	@Override
	public void saveIteration(Iteration iteration) {
		task.setIteration(iteration);
		updateTask();
	}

	@Override
	public void saveCC(List<TaskUserProfile> cc) {
		task.setWatchers(cc);
		updateTask();
	}

	@Override
	public void saveTags(List<Keyword> tags) {
		task.setKeywords(tags);
		updateTask();
	}

	@Override
	public void saveWorkLog(WorkLog value) {
		Iterator<WorkLog> it = task.getWorkLogs().iterator();
		while (it.hasNext()) {
			if (it.next().getId() == null) {
				it.remove(); // Handles case where we previous tried to add, but got a validation failure.
			}
		}
		task.getWorkLogs().add(value);
		updateTask();
	}

	@Override
	public void saveProduct(Product value) {
		task.setProduct(value);

		// Need to get the full product from the repo config.
		for (Product p : repositoryConfiguration.getProducts()) {
			if (p.equals(value)) {
				task.setComponent(p.getDefaultComponent());
				task.setMilestone(p.getDefaultMilestone());
			}
		}
		task.setFoundInRelease(null);
		updateTask();

	}

	@Override
	public void saveComponent(Component value) {
		task.setComponent(value);
		updateTask();
	}

	@Override
	public void saveRelease(Milestone value) {
		task.setMilestone(value);
		updateTask();
	}

	@Override
	public void saveFoundInRelease(String value) {
		task.setFoundInRelease(value);
		updateTask();
	}

	private void updateTask() {
		UpdateTaskAction action = new UpdateTaskAction(projectIdentifier, task);
		getDispatchService().execute(action,
				new AsyncCallbackSupport<UpdateTaskResult>(new OperationMessage("Saving Task...")) {
					@Override
					protected void success(UpdateTaskResult actionResult) {
						Task result = actionResult.get();
						setTask(result);
						taskView.setTask(result);

						if (actionResult.isUpdatedAlready()) {
							getNotifier().displayMessage(
									Message.createErrorMessage("Task has been updated. Review changes and try again."));

						} else {
							getNotifier().displayMessage(Message.createSuccessMessage("Task Saved"));
						}
					}

					@Override
					public void onFailure(Throwable exception) {
						super.onFailure(exception);
						taskView.reEnterEditMode();
					}
				});

	}

	private void populateUi(Task task) {
		if (task.getParentTask() != null) {
			setTaskUrl(projectIdentifier, task.getParentTask());
		}
		setTaskUrls(projectIdentifier, task.getSubTasks());
		setTaskUrls(projectIdentifier, task.getBlocksTasks());

		taskView.setSelf(getAppState().getSelf());
		taskView.setTask(task);
	}

	private void setTask(Task task) {
		this.task = task;
	}

	private void setTaskUrls(String projectIdentifier, List<Task> tasks) {
		if (tasks != null) {
			for (Task task : tasks) {
				setTaskUrl(projectIdentifier, task);
			}
		}
	}

	private void setTaskUrl(String projectIdentifier, Task task) {
		// FIXME should be correct from server eventually
		task.setUrl(ProjectTaskPlace.createPlace(projectIdentifier, task.getId()).getHref());
	}

	@Override
	public String mayStop() {
		if (taskView.isDirty()) {
			return taskMessages.dirtyNavigateWarning();
		}
		return null;
	}

	@Override
	public void saveDueDate(Date value) {
		task.setDeadline(value);
		updateTask();
	}

	@Override
	public void saveDescription(String value) {
		task.setDescription(value);
		updateTask();
	}

	@Override
	public void saveSubTasks(List<Task> value) {
		task.setSubTasks(value);
		updateTask();

	}

	@Override
	public void saveBlocks(List<Task> value) {
		task.setBlocksTasks(value);
		updateTask();

	}

	@Override
	public void saveShortDescription(String text) {
		task.setShortDescription(text);
		updateTask();
	}

	@Override
	public void saveCustomField(String name, String value) {
		task.getCustomFields().put(name, value);
		updateTask();
	}

	@Override
	public void saveTaskType(String value) {
		task.setTaskType(value);
		updateTask();
	}

}
