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
package com.tasktop.c2c.server.tasks.client.widgets.admin.keywords;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.domain.Keyword;

public class ProjectAdminKeywordsEditView extends Composite implements Editor<Keyword>,
		IProjectAdminKeywordsView<IProjectAdminKeywordsView.ProjectAdminKeywordsEditPresenter>, ErrorCapableView {
	interface ProjectAdminKeywordsEditViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminKeywordsEditView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Keyword, ProjectAdminKeywordsEditView> {
	}

	private static ProjectAdminKeywordsEditView instance;
	private static Driver driver = GWT.create(Driver.class);

	public static ProjectAdminKeywordsEditView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminKeywordsEditView();
		}
		return instance;
	}

	private static ProjectAdminKeywordsEditViewUiBinder ourUiBinder = GWT
			.create(ProjectAdminKeywordsEditViewUiBinder.class);
	@UiField
	Button lowerCancelButton;
	@UiField
	Button lowerSaveButton;
	@UiField
	Button upperSaveButton;
	@UiField
	Button upperCancelButton;
	@UiField
	@Path("name")
	TextBox keywordName;
	@UiField
	@Path("description")
	TextArea keywordDescription;

	private ProjectAdminKeywordsEditPresenter presenter;

	private ProjectAdminKeywordsEditView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	public void setPresenter(ProjectAdminKeywordsEditPresenter presenter) {
		this.presenter = presenter;
		driver.edit(presenter.getSelectedKeyword());
	}

	@Override
	public void displayError(String message) {
		this.displayErrors(Collections.singletonList(message));
	}

	@Override
	public void displayErrors(List<String> messages) {
		clearErrors();
		if (messages.size() > 0) {
			ProfileGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage(StringUtils.concatenate(messages)));
		}
	}

	@UiHandler({ "upperSaveButton", "lowerSaveButton" })
	void onSave(ClickEvent event) {
		if (presenter != null) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					// Push any changes from the editors into the object tree.
					driver.flush();
					presenter.onSaveKeyword(ProjectAdminKeywordsEditView.this);
				}
			});
		}
	}

	@UiHandler({ "upperCancelButton", "lowerCancelButton" })
	void onEditCancel(ClickEvent event) {
		if (presenter != null) {
			presenter.onEditCancel();
			clearErrors();
		}
	}

	@Override
	public void clearErrors() {
		// implement
	}
}
