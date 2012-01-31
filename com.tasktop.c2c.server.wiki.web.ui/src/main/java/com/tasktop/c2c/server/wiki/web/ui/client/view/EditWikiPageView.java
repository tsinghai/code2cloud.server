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
package com.tasktop.c2c.server.wiki.web.ui.client.view;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.CompositeClickHandlers;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.Page.GroupAccess;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.EditWikiPagePresenter.EditWikiPageDisplay;

public class EditWikiPageView extends AbstractComposite implements EditWikiPageDisplay, Editor<Page> {

	interface Binder extends UiBinder<Widget, EditWikiPageView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Driver extends SimpleBeanEditorDriver<Page, EditWikiPageView> {
	}

	private static Driver driver = GWT.create(Driver.class);

	@UiField
	@Ignore
	Label pageTitle;
	@UiField
	Button saveButton2;
	@UiField
	Button cancelButton2;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	TextBox path;
	@UiField
	EditWikiPanel content;

	@UiField
	Panel attachmentsPanel;
	@UiField
	FileUpload attachment;
	@UiField
	FormPanel attachmentForm;
	@UiField
	Panel attachmentsListPanel;
	@UiField
	Button attachmentSubmit;

	private Collection<GroupAccess> availableAccessSettings;

	private static final class GroupAccessRenderer implements Renderer<GroupAccess> {

		static GroupAccessRenderer INSTANCE = new GroupAccessRenderer();

		private GroupAccessRenderer() {

		}

		@Override
		public String render(GroupAccess object) {
			if (object == null) {
				return "None";
			}
			return object.getFriendlyName();
		}

		@Override
		public void render(GroupAccess object, Appendable appendable) throws IOException {
			if (object != null) {
				appendable.append(object.getFriendlyName());
			}
		}

	}

	@UiField
	Panel deleteAccessPanel;
	@UiField(provided = true)
	ValueListBox<GroupAccess> deleteAccess = new ValueListBox<Page.GroupAccess>(GroupAccessRenderer.INSTANCE);
	@UiField(provided = true)
	ValueListBox<GroupAccess> editAccess = new ValueListBox<Page.GroupAccess>(GroupAccessRenderer.INSTANCE);

	private Page page;

	public EditWikiPageView() {
		initWidget(uiBinder.createAndBindUi(this));
		content.setPath(path);
		attachmentForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		attachmentForm.setMethod(FormPanel.METHOD_POST);

		attachmentSubmit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				attachmentForm.submit();
			}
		});
		new CompositeClickHandlers(cancelButton, cancelButton2).addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				isEditing = false;
			}
		});

		driver.initialize(this);
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public void setPage(Page page) {
		this.page = page;
		// null page or new page obj
		if (page == null || page.getId() == null) {
			pageTitle.setText("New Page");
			attachmentsPanel.setVisible(false);
		} else {
			pageTitle.setText("Edit Page");
			attachmentsPanel.setVisible(true);
			attachmentForm.setAction(page.getAttachmentsUrl());

			// If the delete value is already beyond our permissions, then we can't edit it, so dont' display it.
			boolean canEditDelete = availableAccessSettings.contains(page.getDeleteAccess());
			deleteAccessPanel.setVisible(canEditDelete);
		}

		driver.edit(page);
		isEditing = true;

		// This must be done after the setValue to avoid null getting inside;
		deleteAccess.setAcceptableValues(availableAccessSettings);
		editAccess.setAcceptableValues(availableAccessSettings);

	}

	@Override
	public void setAttachments(List<Attachment> attachments) {
		attachmentsListPanel.clear();
		if (attachments != null && !attachments.isEmpty()) {
			for (Attachment attachment : attachments) {
				WikiAttachmentView wikiAttachmentView = new WikiAttachmentView(attachment);
				attachmentsListPanel.add(wikiAttachmentView);
			}
		}
	}

	@Override
	public void setProjectIdentifier(String projectIdentifier) {
		content.setProjectId(projectIdentifier);
	}

	public Page getPage() {
		updateModel();
		return page;
	}

	protected void updateModel() {
		driver.flush();
		isEditing = false;
	}

	@Override
	public void addSaveClickHandler(final ClickHandler clickHandler) {
		saveButton2.addClickHandler(clickHandler);
		saveButton.addClickHandler(clickHandler);
	}

	@Override
	public void addCancelClickHandler(ClickHandler clickHandler) {
		cancelButton2.addClickHandler(clickHandler);
		cancelButton.addClickHandler(clickHandler);
	}

	@Override
	public void addAttachmentSubmitCompleteHandler(SubmitCompleteHandler handler) {
		attachmentForm.addSubmitCompleteHandler(handler);
	}

	@Override
	/**
	 * @param availableAccessSettings
	 *            the availableAccessSettings to set
	 */
	public void setAvailableAccessSettings(Collection<GroupAccess> availableAccessSettings) {
		this.availableAccessSettings = availableAccessSettings;
	}

	@Override
	public String getAttachmentFileName() {
		String path = attachment.getFilename();
		int i = path.lastIndexOf("/");
		if (i == -1) {
			i = path.lastIndexOf("\\");
		}
		if (i != -1) {
			path = path.substring(i + 1);
		}
		return path;
	}

	@Override
	public void clearAttachementForm() {
		attachmentForm.reset();
	}

	private boolean isEditing;

	@Override
	public boolean isDirty() {
		if (!isEditing) {
			return false;
		}
		return driver.isDirty();
	}
}
