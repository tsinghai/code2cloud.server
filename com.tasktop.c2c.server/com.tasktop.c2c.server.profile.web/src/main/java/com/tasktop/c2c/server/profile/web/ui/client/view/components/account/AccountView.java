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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.authentication.AuthenticationView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.place.AccountAuthenticationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.place.AccountNotificationsPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.place.AccountProfilePlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.profile.AccountNotificationsView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.profile.AccountProfileView;

public class AccountView extends Composite {
	interface AccountViewUiBinder extends UiBinder<HTMLPanel, AccountView> {
	}

	private static AccountView instance;

	public static AccountView getInstance() {
		if (instance == null) {
			instance = new AccountView();
		}
		return instance;
	}

	private static AccountViewUiBinder ourUiBinder = GWT.create(AccountViewUiBinder.class);
	private static String SELECTED = "selected";

	@UiField
	SimplePanel contentPanel;
	@UiField
	Anchor profileSelector;
	@UiField
	Anchor authenticationSelector;
	@UiField
	Anchor notificationsSelector;

	private IAccountView.AccountPresenter presenter;
	private Widget selected = null;

	public AccountView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void setPresenter(IAccountView.AccountPresenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("profileSelector")
	void onSelectProfile(ClickEvent event) {
		presenter.goTo(new AccountProfilePlace());
	}

	@UiHandler("authenticationSelector")
	void onSelectAuthentication(ClickEvent event) {
		presenter.goTo(new AccountAuthenticationPlace(false, false));
	}

	@UiHandler("notificationsSelector")
	void onSelectNotifications(ClickEvent event) {
		presenter.goTo(new AccountNotificationsPlace());
	}

	public void goTo(Place place) {
		if (place instanceof AccountProfilePlace) {
			changeSelected(profileSelector);
			AccountProfileView.getInstance().setPresenter(presenter);
			contentPanel.setWidget(AccountProfileView.getInstance());
		} else if (place instanceof AccountAuthenticationPlace) {
			changeSelected(authenticationSelector);
			AuthenticationView.getInstance().setPresenter(presenter);
			contentPanel.setWidget(AuthenticationView.getInstance());
		} else if (place instanceof AccountNotificationsPlace) {
			changeSelected(notificationsSelector);
			AccountNotificationsView.getInstance().setPresenter(presenter);
			contentPanel.setWidget(AccountNotificationsView.getInstance());
		}
	}

	private void changeSelected(Widget toSelect) {
		if (selected != null) {
			selected.removeStyleName(SELECTED);
		}
		selected = toSelect;
		selected.addStyleName(SELECTED);
	}
}
