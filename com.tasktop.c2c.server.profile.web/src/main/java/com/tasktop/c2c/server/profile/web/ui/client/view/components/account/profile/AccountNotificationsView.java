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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.NotificationSettings;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AccountNotificationsView extends AbstractComposite implements Editor<NotificationSettings>,
		IAccountView<IAccountView.AccountProfilePresenter> {

	interface AccountNotificationsViewUiBinder extends UiBinder<Widget, AccountNotificationsView> {
	}

	private static AccountNotificationsView instance;

	public static AccountNotificationsView getInstance() {
		if (instance == null) {
			instance = new AccountNotificationsView();
		}
		return instance;
	}

	private static AccountNotificationsViewUiBinder ourUiBinder = GWT.create(AccountNotificationsViewUiBinder.class);
	@UiField
	Panel panel;
	// @UiField
	AccountNotificationsReadOnlyView accountNotificationsReadOnlyView = new AccountNotificationsReadOnlyView();
	// @UiField
	AccountNotificationsEditView accountNotificationsEditView = new AccountNotificationsEditView();

	public AccountNotificationsView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		setEditMode(false);
		panel.add(accountNotificationsEditView);
		panel.add(accountNotificationsReadOnlyView);
		accountNotificationsReadOnlyView.setOnEdit(new Runnable() {

			@Override
			public void run() {
				setEditMode(true);
			}
		});
		accountNotificationsEditView.setOnCancel(new Runnable() {

			@Override
			public void run() {
				setEditMode(false);
			}
		});
		accountNotificationsEditView.setOnSave(new Runnable() {

			@Override
			public void run() {
				setEditMode(false);
				accountNotificationsReadOnlyView.update();
			}
		});
	}

	private void setEditMode(boolean editMode) {
		accountNotificationsReadOnlyView.setVisible(!editMode);
		accountNotificationsEditView.setVisible(editMode);
	}

	@Override
	public void setPresenter(AccountProfilePresenter presenter) {
		accountNotificationsReadOnlyView.setPresenter(presenter);
		accountNotificationsEditView.setPresenter(presenter);
	}

}
