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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AccountProfileReadOnlyView extends AbstractAccountProfileView implements Editor<Profile>,
		IAccountView<IAccountView.AccountProfilePresenter> {

	interface ProfileViewUiBinder extends UiBinder<HTMLPanel, AccountProfileReadOnlyView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Profile, AccountProfileReadOnlyView> {
	}

	private static AccountProfileReadOnlyView instance;

	public static AccountProfileReadOnlyView getInstance() {
		if (instance == null) {
			instance = new AccountProfileReadOnlyView();
		}
		return instance;
	}

	private static ProfileViewUiBinder ourUiBinder = GWT.create(ProfileViewUiBinder.class);
	@UiField
	@Ignore
	Anchor editAnchor;

	@UiField
	@Path("username")
	Label userNameField;
	@UiField
	@Path("email")
	Label emailField;
	@UiField
	@Path("firstName")
	Label firstNameField;
	@UiField
	@Path("lastName")
	Label lastNameField;
	static Driver driver = GWT.create(Driver.class);

	public AccountProfileReadOnlyView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	@Override
	public void setPresenter(AccountProfilePresenter presenter) {
		super.setPresenter(presenter);
		driver.edit(presenter.getProfile());
	}
}
