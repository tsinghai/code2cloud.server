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

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractValueChooser;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.DuplicateTaskSuggestOracle;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;

public class StatusEditorView extends Composite implements ValueAwareEditor<Task> {
	interface Binder extends UiBinder<Widget, StatusEditorView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField(provided = true)
	public ValueListBox<TaskStatus> status = new ValueListBox<TaskStatus>(ReferenceValueRenderer.getInstance());
	@UiField(provided = true)
	public ValueListBox<TaskResolution> resolution = new ValueListBox<TaskResolution>(
			ReferenceValueRenderer.getInstance());
	@UiField(provided = true)
	@Ignore
	public SuggestBox duplicateOf;

	private RepositoryConfiguration repositoryConfiguration;;
	private DuplicateTaskSuggestOracle taskSuggestOracle = new DuplicateTaskSuggestOracle();
	private boolean isNewTask = false;

	public StatusEditorView() {
		duplicateOf = AbstractValueChooser.createSuggestBox(taskSuggestOracle);
		duplicateOf.setLimit(10);
		initWidget(uiBinder.createAndBindUi(this));
		status.addValueChangeHandler(new ValueChangeHandler<TaskStatus>() {

			@Override
			public void onValueChange(ValueChangeEvent<TaskStatus> event) {
				onStatusChange(event.getValue());

			}
		});
		resolution.addValueChangeHandler(new ValueChangeHandler<TaskResolution>() {

			@Override
			public void onValueChange(ValueChangeEvent<TaskResolution> event) {
				onResolutionChanged(event.getValue());
			}
		});
	}

	public void setStylesForInline() {
		duplicateOf.setStyleName("mleft40");
		status.setStyleName("left");
		resolution.setStyleName("mleft40");
	}

	public void setProjectIdentifier(String projectIdentifier) {
		taskSuggestOracle.setProjectIdentifier(projectIdentifier);
	}

	private void setTask(Task task) {
		status.setValue(task.getStatus());
		resolution.setValue(task.getResolution());

		if (isNewTask) {
			status.setAcceptableValues(repositoryConfiguration.computeValidStatuses(null));
		} else {
			status.setAcceptableValues(repositoryConfiguration.computeValidStatuses(task.getStatus()));
		}
		resolution.setAcceptableValues(repositoryConfiguration.computeValidResolutions(task.getStatus()));

		if (task.getDuplicateOf() != null) {
			duplicateOf.setValue(task.getDuplicateOf().getId().toString());
		} else {
			duplicateOf.setValue("");
		}
		updateStatusEditorViewVisibility();
	}

	public RepositoryConfiguration getRepositoryConfiguration() {
		return repositoryConfiguration;
	}

	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
	}

	private void updateStatusEditorViewVisibility() {
		String statusString = getSelectedStatus().getValue();
		if (statusString.equalsIgnoreCase("RESOLVED")) {
			resolution.setVisible(true);
			if (resolution.getValue().isDuplicate()) {
				duplicateOf.setVisible(true);
			} else {
				duplicateOf.setVisible(false);
			}
		} else {
			resolution.setVisible(false);
			duplicateOf.setVisible(false);
		}

	}

	private void onStatusChange(TaskStatus selectedStatus) {
		updateStatusEditorViewVisibility();

		if (selectedStatus != null && selectedStatus.getValue().equalsIgnoreCase("RESOLVED")) {
			TaskResolution selectedResolution = getSelectedResolution();
			if (selectedResolution == null || isEmpty(selectedResolution.getValue())) {
				resolution.setValue(repositoryConfiguration.getDefaultResolution());
			}
		}

		resolution.setAcceptableValues(repositoryConfiguration.computeValidResolutions(selectedStatus));

		resolution.getElement().focus();
	}

	private void onResolutionChanged(TaskResolution value) {
		updateStatusEditorViewVisibility();
		if (duplicateOf.isVisible()) {
			duplicateOf.setFocus(true);
		}
	}

	private boolean isEmpty(String value) {
		return value == null || value.length() == 0;
	}

	// FIXME This has been duplicated for View Task in TaskPresenter.saveStatus()
	public void updateTask(Task task) {
		if (task.getStatus().isOpen()) {
			task.setResolution(null);
			task.setDuplicateOf(null);
		} else {
			if (task.getResolution() != null && task.getResolution().isDuplicate()) {
				try {
					task.setDuplicateOf(new Task());
					task.getDuplicateOf().setId(Integer.parseInt(duplicateOf.getText()));
				} catch (NumberFormatException e) {
					task.setDuplicateOf(null);
				}
			} else {
				task.setDuplicateOf(null);
			}
		}
	}

	public TaskStatus getSelectedStatus() {
		return status.getValue();
	}

	public TaskResolution getSelectedResolution() {
		return resolution.getValue();
	}

	public Integer getDuplicateId() {
		if (duplicateOf.getText() == null || duplicateOf.getText().isEmpty()) {
			return null;
		}
		return Integer.parseInt(duplicateOf.getText());
	}

	private Task task;

	@Override
	public void setValue(Task value) {
		this.task = value;
		setTask(task);
	}

	@Override
	public void setDelegate(EditorDelegate<Task> delegate) {
	}

	@Override
	public void flush() {
		updateTask(task);
	}

	@Override
	public void onPropertyChange(String... paths) {

	}

	/**
	 * @return the isNewTask
	 */
	public boolean isNewTask() {
		return isNewTask;
	}

	/**
	 * @param isNewTask
	 *            the isNewTask to set
	 */
	public void setNewTask(boolean isNewTask) {
		this.isNewTask = isNewTask;
	}

}
