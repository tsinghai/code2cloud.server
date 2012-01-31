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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AccountProfileView extends Composite implements Editor<Profile>,
		IAccountView<IAccountView.AccountProfilePresenter> {

	interface ProfileViewUiBinder extends UiBinder<HTMLPanel, AccountProfileView> {
	}

	private static AccountProfileView instance;

	public static AccountProfileView getInstance() {
		if (instance == null) {
			instance = new AccountProfileView();
		}
		return instance;
	}

	private static ProfileViewUiBinder ourUiBinder = GWT.create(ProfileViewUiBinder.class);

	@UiField
	FlowPanel panel;

	public AccountProfileView() {
		initWidget(ourUiBinder.createAndBindUi(this));
		panel.add(AccountProfileReadOnlyView.getInstance());
		panel.add(AccountProfileEditView.getInstance());

		AccountProfileReadOnlyView.getInstance().editAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				setEditMode(true);
			}
		});

		AccountProfileEditView.getInstance().cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				setEditMode(false);
			}
		});
	}

	public void setEditMode(boolean editMode) {
		AccountProfileReadOnlyView.getInstance().setVisible(!editMode);
		AccountProfileEditView.getInstance().setVisible(editMode);
	}

	@Override
	public void setPresenter(AccountProfilePresenter presenter) {
		AccountProfileEditView.getInstance().setPresenter(presenter);
		AccountProfileReadOnlyView.getInstance().setPresenter(presenter);
		setEditMode(false);
	}

}
