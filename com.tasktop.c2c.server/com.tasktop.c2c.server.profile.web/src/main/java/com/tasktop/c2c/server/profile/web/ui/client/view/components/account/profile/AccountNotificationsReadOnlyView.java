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
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.project.NotificationSettings;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AccountNotificationsReadOnlyView extends Composite implements Editor<NotificationSettings>,
		IAccountView<IAccountView.AccountProfilePresenter> {

	interface AccountNotificationsViewUiBinder extends UiBinder<Widget, AccountNotificationsReadOnlyView> {
	}

	private static AccountNotificationsViewUiBinder ourUiBinder = GWT.create(AccountNotificationsViewUiBinder.class);

	@UiField
	SpanElement emailTaskActivityCheckboxSpan;

	@UiField
	SpanElement emailNewsAndEventsCheckboxSpan;

	@UiField
	SpanElement emailServiceAndMaintenanceCheckboxSpan;

	@UiField
	Label emailLabel;

	Runnable onEdit;

	private AccountProfilePresenter presenter;

	public AccountNotificationsReadOnlyView() {
		initWidget(ourUiBinder.createAndBindUi(this));

	}

	@Override
	public void setPresenter(AccountProfilePresenter presenter) {
		this.presenter = presenter;
		update();
	}

	/**
	 * @param notificationSettings
	 */
	private void setValue(Profile profile) {
		emailLabel.setText(profile.getEmail());
		updateCBSpanStyle(emailTaskActivityCheckboxSpan, profile.getNotificationSettings().getEmailTaskActivity());
		updateCBSpanStyle(emailNewsAndEventsCheckboxSpan, profile.getNotificationSettings().getEmailNewsAndEvents());
		updateCBSpanStyle(emailServiceAndMaintenanceCheckboxSpan, profile.getNotificationSettings()
				.getEmailServiceAndMaintenance());

	}

	private void updateCBSpanStyle(SpanElement span, boolean checked) {
		if (checked) {
			span.addClassName("active");
		} else {
			span.removeClassName("active");
		}
	}

	@UiHandler("editButton")
	void onEdit(ClickEvent event) {
		onEdit.run();
	}

	void setOnEdit(Runnable onEdit) {
		this.onEdit = onEdit;
	}

	/**
	 * 
	 */
	public void update() {
		setValue(presenter.getProfile());
	}

}
