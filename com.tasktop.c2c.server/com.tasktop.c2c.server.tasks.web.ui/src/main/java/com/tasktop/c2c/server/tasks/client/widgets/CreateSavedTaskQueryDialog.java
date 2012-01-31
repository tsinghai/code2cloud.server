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
package com.tasktop.c2c.server.tasks.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CreateSavedTaskQueryDialog extends DialogBox {
	interface Binder extends UiBinder<Widget, CreateSavedTaskQueryDialog> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	TextBox name;

	@UiField
	Button saveButton;

	public CreateSavedTaskQueryDialog() {
		super(true, true);
		super.setText("Save Query");
		super.setAnimationEnabled(true);
		super.setGlassEnabled(false);
		super.setWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent e) {
		this.hide();
	}

	@Override
	public void show() {
		name.setText("");
		super.show();
	}

}
