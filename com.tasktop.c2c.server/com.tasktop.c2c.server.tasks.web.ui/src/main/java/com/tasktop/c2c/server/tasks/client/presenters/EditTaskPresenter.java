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

import static com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil.toTaskUserProfile;

import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.ProjectEditTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.widgets.EditTaskDisplay;
import com.tasktop.c2c.server.tasks.client.widgets.EditTaskView;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentUploadUtil;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.shared.action.UpdateTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateTaskResult;

public class EditTaskPresenter extends AbstractEditTaskPresenter<EditTaskDisplay> implements SplittableActivity {

	public interface AttachmentDisplay {
		void addSubmitCompleteHandler(FormPanel.SubmitCompleteHandler handler);

		void setValueHiddenTaskValue(String createTaskHandleValue);

		void resetForm();

		void setAttachments(List<Attachment> attachments);

		List<Attachment> getAttachments();

		void setProjectIdentifier(String projectIdentifier);
	}

	public EditTaskPresenter(EditTaskDisplay view) {
		super(view);

	}

	public EditTaskPresenter() {
		this(new EditTaskView());
	}

	public void setPlace(Place p) {
		ProjectEditTaskPlace place = (ProjectEditTaskPlace) p;
		task = place.getTask();
		projectIdentifier = place.getProjectId();
		repositoryConfiguration = place.getRepositoryConfiguration();
		editTaskView.getAttachmentDisplay().setProjectIdentifier(projectIdentifier);
	}

	@Override
	protected void fetchTask() {
		populateUi(task);
	}

	@Override
	protected void doCancel() {
		ProjectTaskPlace.createPlace(projectIdentifier, task.getId()).go();
	}

	@Override
	protected void doSaveTask() {
		populateModel();

		UpdateTaskAction action = new UpdateTaskAction(projectIdentifier, task);
		getDispatchService().execute(action,
				new AsyncCallbackSupport<UpdateTaskResult>(new OperationMessage("Saving Task...")) {

					@Override
					protected void success(UpdateTaskResult actionResult) {
						Task result = actionResult.get();

						if (actionResult.isUpdatedAlready()) {
							task = result;
							populateUi(task);
							getNotifier().displayMessage(
									Message.createErrorMessage("Task has been updated. Review changes and try again."));
						} else {
							ProjectTaskPlace place = ProjectTaskPlace.createPlaceWithTask(projectIdentifier, result);
							place.displayOnArrival(Message.createSuccessMessage("Task Saved"));
							place.go();
						}
					}
				});
	}

	@Override
	public void populateUi(Task task) {
		super.populateUi(task);
		editTaskView.setSelf(getAppState().getSelf());
		editTaskView.setProjectIdentifier(projectIdentifier);
		editTaskView.setValue(task);
		if (task.getAssignee() != null) {
			ownerSet = true; // Prevent overwriting on component change.
		}
	}

	@Override
	public void bind() {
		super.bind();

		editTaskView.getAttachmentDisplay().addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String json = event.getResults();
				JSONValue value = JSONParser.parseLenient(json);
				JSONObject uploadResult = value.isObject().get("uploadResult").isObject();
				if (uploadResult != null) {
					JSONString errorMessage = uploadResult.get("errorMessage").isString();
					if (errorMessage != null) {
						ProfileGinjector.get.instance().getNotifier()
								.displayMessage(Message.createErrorMessage(errorMessage.stringValue()));
					} else {
						// update the taskHandle for the task
						TaskHandle taskHandle = buildTaskHandle(uploadResult);
						task.setTaskHandle(taskHandle);
						// update the the hidden taskHandle field
						editTaskView.getAttachmentDisplay().setValueHiddenTaskValue(
								AttachmentUploadUtil.createTaskHandleValue(taskHandle));

						// update the attachments table
						JSONArray attachments = uploadResult.get("attachments").isArray();
						JSONObject attachmentJSON = attachments.get(0).isObject();
						Attachment newAttachment = buildAttachment(attachmentJSON);
						addAttachment(newAttachment);

						// reset the attachment upload form
						editTaskView.getAttachmentDisplay().resetForm();
					}
				}
			}

			private TaskHandle buildTaskHandle(JSONObject uploadResult) {
				JSONObject taskHandleJSON = uploadResult.get("taskHandle").isObject();
				TaskHandle taskHandle = new TaskHandle();
				taskHandle.setId(Integer.valueOf(taskHandleJSON.get("id").isNumber().toString()));
				taskHandle.setVersion(taskHandleJSON.get("version").isString().stringValue());
				return taskHandle;
			}

			private Attachment buildAttachment(JSONObject attachmentJSON) {
				Attachment newAttachment = new Attachment();
				newAttachment.setId(Integer.valueOf(attachmentJSON.get("id").isNumber().toString()));
				newAttachment.setFilename(attachmentJSON.get("filename").isString().stringValue());
				newAttachment.setDescription(attachmentJSON.get("description").isString().stringValue());
				newAttachment.setByteSize(Integer.valueOf(attachmentJSON.get("byteSize").isNumber().toString()));
				newAttachment.setUrl(attachmentJSON.get("url").isString().stringValue());
				TaskUserProfile taskUserProfile = toTaskUserProfile(getAppState().getSelf());
				newAttachment.setSubmitter(taskUserProfile);
				return newAttachment;
			}
		});

	}

	public void setAttachments(List<Attachment> attachments) {
		editTaskView.getAttachmentDisplay().setAttachments(attachments);
	}

	public void addAttachment(Attachment attachment) {
		List<Attachment> attachments = editTaskView.getAttachmentDisplay().getAttachments();
		attachments.add(attachment);
		setAttachments(attachments);
	}

	@Override
	public void configure(RepositoryConfiguration configuration) {
		super.configure(configuration);
		editTaskView.setRepositoryConfiguration(configuration);

	}
}
