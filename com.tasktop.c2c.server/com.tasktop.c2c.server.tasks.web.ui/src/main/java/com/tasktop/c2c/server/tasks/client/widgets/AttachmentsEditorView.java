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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.tasks.client.presenters.EditTaskPresenter;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentUploadUtil;
import com.tasktop.c2c.server.tasks.domain.Task;

public class AttachmentsEditorView extends AbstractComposite implements EditTaskPresenter.AttachmentDisplay {
	protected interface Binder extends UiBinder<Widget, AttachmentsEditorView> {
	}

	protected static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public Panel attachmentsPanel;
	@UiField
	public FormPanel attachmentForm;
	@UiField
	public FileUpload attachment;
	@UiField
	public TextBox attachmentDescription;
	@UiField
	public Hidden hiddenTaskHandle;

	@UiField
	public Button attachmentSubmitButton;

	private List<Attachment> attachments;
	public Person currenUser;

	public AttachmentsEditorView() {
		initWidget(uiBinder.createAndBindUi(this));

		attachmentForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		attachmentForm.setMethod(FormPanel.METHOD_POST);
		attachment.setName("attachment0");
		attachmentDescription.setName("description0");
		hiddenTaskHandle.setName(AttachmentUploadUtil.TASK_HANDLE_FORM_NAME);

		attachmentSubmitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				attachmentForm.submit();
			}
		});
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
		updateUi();
	}

	public void setTask(Task task) {
		hiddenTaskHandle.setValue(AttachmentUploadUtil.createTaskHandleValue(task.getTaskHandle()));
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void updateUi() {
		attachmentsPanel.clear();
		if (attachments.size() > 0) {
			for (int i = 0; i < attachments.size(); i++) {
				Attachment attachment = attachments.get(i);
				Panel attachmentPanel = new FlowPanel();
				attachmentPanel.setStyleName("file-uploaded");
				attachmentPanel.add(new HTML("<span class=\"file-icon\"/>"));
				attachmentPanel.add(new Anchor(attachment.getFilename(), attachment.getUrl()));
				// table.setHTML(i, 1, attachment.getDescription());
				attachmentPanel.add(new Label("   ("
						+ String.valueOf(Math.round(Float.valueOf(attachment.getByteSize()) / 1024)) + " KB)"));
				// PersonLabel personLabel = new PersonLabel();
				// personLabel.setPerson(toPerson(attachment.getSubmitter()));
				// personLabel.setAsSelf(toPerson(attachment.getSubmitter()).equals(currenUser));
				// table.setWidget(i, 2, personLabel);
				// table.setHTML(i, 3, stringValueDateTime(attachment.getCreationDate()));
				attachmentsPanel.add(attachmentPanel);
			}

		}
	}

	@Override
	public void addSubmitCompleteHandler(SubmitCompleteHandler handler) {
		attachmentForm.addSubmitCompleteHandler(handler);
	}

	@Override
	public void setValueHiddenTaskValue(String createTaskHandleValue) {
		hiddenTaskHandle.setValue(createTaskHandleValue);
	}

	@Override
	public void resetForm() {
		attachmentForm.reset();
	}

	@Override
	public void setProjectIdentifier(String applicatoinId) {
		String path = Window.Location.getPath();
		String appCtx = path.substring(0, path.lastIndexOf("/"));
		// REVIEW Should source the url from elsewhere
		String actionUrl = appCtx + "/s/" + applicatoinId + "/tasks/upload/";
		attachmentForm.setAction(actionUrl);
	}
}
