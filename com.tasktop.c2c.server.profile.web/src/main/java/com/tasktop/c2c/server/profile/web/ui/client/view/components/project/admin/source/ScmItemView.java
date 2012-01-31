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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

public class ScmItemView extends Composite {

	interface Binder extends UiBinder<Widget, ScmItemView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label urlLabel;
	@UiField
	Anchor removeAnchor;

	public ScmItemView(ScmRepository repo) {
		initWidget(uiBinder.createAndBindUi(this));
		urlLabel.setText(repo.getUrl());
	}
}
