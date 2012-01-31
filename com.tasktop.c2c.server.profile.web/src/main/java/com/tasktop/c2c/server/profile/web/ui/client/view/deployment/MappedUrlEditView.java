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
package com.tasktop.c2c.server.profile.web.ui.client.view.deployment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class MappedUrlEditView extends Composite {

	interface Binder extends UiBinder<Widget, MappedUrlEditView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	TextBox url;
	@UiField
	Anchor removeButton;
	@UiField
	Anchor addUrlButton;

	public MappedUrlEditView(String url) {
		initWidget(uiBinder.createAndBindUi(this));
		this.url.setText(url);
	}

	public void displayAddButton(boolean show) {
		addUrlButton.setVisible(show);
	}
}
