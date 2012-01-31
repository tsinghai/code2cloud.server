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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.profile;


import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.tasktop.c2c.server.profile.domain.project.NotificationSettings;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AccountNotificationsEditView extends Composite implements Editor<NotificationSettings>,
		IAccountView<IAccountView.AccountProfilePresenter> {

	interface AccountNotificationsViewUiBinder extends UiBinder<HTMLPanel, AccountNotificationsEditView> {
	}

	interface Driver extends SimpleBeanEditorDriver<NotificationSettings, AccountNotificationsEditView> {
	}

	private static AccountNotificationsViewUiBinder ourUiBinder = GWT.create(AccountNotificationsViewUiBinder.class);

	@UiField
	@Ignore
	Label emailLabel;

	@UiField
	@Path("emailTaskActivity")
	CheckBox emailTaskActivityField;

	@UiField
	@Path("emailNewsAndEvents")
	CheckBox emailNewsAndEventsField;

	@UiField
	@Path("emailServiceAndMaintenance")
	CheckBox emailServiceAndMaintenanceField;

	private AccountProfilePresenter presenter;
	private static Driver driver = GWT.create(Driver.class);

	private Runnable onCancel;
	private Runnable onSave;

	public AccountNotificationsEditView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	@Override
	public void setPresenter(AccountProfilePresenter presenter) {
		this.presenter = presenter;
		driver.edit(presenter.getProfile().getNotificationSettings());
		emailLabel.setText(presenter.getProfile().getEmail());
	}

	@UiHandler("saveButton")
	void onSave(ClickEvent event) {
		if (driver.isDirty()) {
			driver.flush();
			presenter.saveProfile();
		}
		onSave.run();
	}

	@UiHandler("cancelButton")
	void onEditCancel(ClickEvent event) {
		driver.edit(presenter.getProfile().getNotificationSettings());
		onCancel.run();
	}

	/**
	 * @param onCancel
	 *            the onCancel to set
	 */
	public void setOnCancel(Runnable onCancel) {
		this.onCancel = onCancel;
	}

	/**
	 * @param onSave
	 *            the onSave to set
	 */
	public void setOnSave(Runnable onSave) {
		this.onSave = onSave;
	}
}
