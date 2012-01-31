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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.HelpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignOutPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.UserAccountPlace;

public class UserMenuPopupPanel extends PopupPanel {

	interface UserMenuPopupPanelUiBinder extends UiBinder<Widget, UserMenuPopupPanel> {
	}

	private static UserMenuPopupPanelUiBinder uiBinder = GWT.create(UserMenuPopupPanelUiBinder.class);

	@UiField
	Anchor account;
	@UiField
	Anchor help;
	@UiField
	Anchor signOut;

	public UserMenuPopupPanel() {
		super(true);

		setWidget(uiBinder.createAndBindUi(this));
		setAutoHideOnHistoryEventsEnabled(true);

		account.setHref(UserAccountPlace.createPlace().getHref());
		help.setHref(HelpPlace.createPlace().getHref());
		signOut.setHref(SignOutPlace.createPlace().getHref());
		signOut.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		AppGinjector.get.instance().getEventBus().addHandler(ScrollEvent.getType(), new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				hide();
			}
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				hide();
			}
		});
	}
}
