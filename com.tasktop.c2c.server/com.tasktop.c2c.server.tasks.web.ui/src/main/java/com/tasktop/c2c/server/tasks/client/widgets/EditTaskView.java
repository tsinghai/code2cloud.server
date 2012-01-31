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

import java.math.BigDecimal;


import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;
import com.tasktop.c2c.server.tasks.client.presenters.EditTaskPresenter;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.ExternalTaskRelationChooser;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.ExternalTaskRelationFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.ParentTaskChooser;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.TaskCompositeFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.TaskSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

public class EditTaskView extends AbstractEditTaskView implements EditTaskDisplay {

	interface EditTaskViewUiBinder extends UiBinder<Widget, EditTaskView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Task, EditTaskView> {
	}

	private static EditTaskViewUiBinder uiBinder = GWT.create(EditTaskViewUiBinder.class);

	private TaskSuggestOracle subtaskOracle = new TaskSuggestOracle();
	private TaskSuggestOracle parentTaskOracle = new TaskSuggestOracle();

	@UiField(provided = true)
	protected MultiValueChooser<Task> subTasks = new MultiValueChooser<Task>(subtaskOracle);
	@UiField(provided = true)
	protected ParentTaskChooser blocksTasks = new ParentTaskChooser(parentTaskOracle);
	@UiField(provided = true)
	protected ExternalTaskRelationChooser externalTaskRelations = new ExternalTaskRelationChooser();

	@UiField
	@Ignore
	protected Label parentTaskLabel;
	@UiField
	@Ignore
	protected Label subTaskLabel;
	@UiField
	@Ignore
	protected Label externalTaskRelationsLabel;

	@UiField
	protected AttachmentsEditorView attachmentsEditorView;

	@UiField
	protected CommentsPanel commentsPanel;

	// Work logs
	@UiField
	protected WorkLogEditor workLogEditor;
	@UiField
	protected Panel workLogs;
	@UiField
	@Ignore
	protected Label workLogTotal;

	@UiField
	@Ignore
	protected Label editHeader;

	private Driver driver = GWT.create(Driver.class);
	private boolean isEditing = false;

	public EditTaskView() {
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
		commentsPanel.postComment.setVisible(false);
		configChoosers();
	}

	private void configChoosers() {
		subTasks.setValueCompositeFactory(new TaskCompositeFactory());
		subTasks.addLabel(subTaskLabel);

		blocksTasks.setValueCompositeFactory(new TaskCompositeFactory());
		blocksTasks.addLabel(parentTaskLabel);

		externalTaskRelations.setValueCompositeFactory(new ExternalTaskRelationFactory());
		externalTaskRelations.addLabel(externalTaskRelationsLabel);
	}

	@Override
	public void setSelf(Person self) {
		super.setSelf(self);
		attachmentsEditorView.currenUser = self;
		commentsPanel.setSelf(self);
	}

	@Override
	public void setProjectIdentifier(String projectIdentifier) {
		super.setProjectIdentifier(projectIdentifier);
		parentTaskOracle.setProjectIdentifier(projectIdentifier);
		subtaskOracle.setProjectIdentifier(projectIdentifier);
		statusEditor.setProjectIdentifier(projectIdentifier);
		commentsPanel.setProjectIdentifier(projectIdentifier);
	}

	@Override
	protected void setTask(Task task) {
		super.setTask(task);
		driver.edit(task);
		isEditing = true;
		editHeader.setText("Edit Task " + task.getId().toString());

		blocksTasks.setOrigin(null);
		blocksTasks.setUnremovableValues(null);

		subTasks.setOrigin(null);
		subTasks.setUnremovableValues(null);

		externalTaskRelations.setOrigin(null);
		externalTaskRelations.setUnremovableValues(null);

		attachmentsEditorView.setAttachments(task.getAttachments());
		attachmentsEditorView.setTask(task);
		commentsPanel.setValue(task);
		workLogEditor.clear();

		workLogs.clear();
		BigDecimal sum = BigDecimal.ZERO;
		for (WorkLog worklog : task.getWorkLogs()) {
			workLogs.add(new WorkLogItem(worklog));
			sum = sum.add(worklog.getHoursWorked());
		}
		workLogTotal.setText(TimePeriodRenderer.HOUR_RENDERER.render(sum));

	}

	@Override
	protected void populateTask(Task task) {
		super.populateTask(task);
		commentsPanel.populateTask(task);
		if (workLogEditor.getValue() != null) {
			task.getWorkLogs().clear(); // don't need to resubmit old worklogs
			WorkLog workLog = workLogEditor.getValue();
			workLog.setProfile(PersonUtil.toTaskUserProfile(self));
			// if this appears to be a new worklog add it to the task
			if (workLog.getDateWorked() != null && workLog.getHoursWorked() != null) {
				task.getWorkLogs().add(workLog);
			}
		}
		driver.flush();
		isEditing = false;
	}

	@Override
	public EditTaskPresenter.AttachmentDisplay getAttachmentDisplay() {
		return attachmentsEditorView;
	}

	@Override
	protected boolean areEditorsDirty() {
		return super.areEditorsDirty() || driver.isDirty() || commentsPanel.isDirty();
	}

}
