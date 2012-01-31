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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;

public class TeamMemberView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, TeamMemberView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public Image icon;
	@UiField
	public Label name;
	@UiField
	public Label username;

	@UiField
	public Anchor rolesAnchor;

	@UiField
	public Image removeButton;

	public TeamMemberView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
