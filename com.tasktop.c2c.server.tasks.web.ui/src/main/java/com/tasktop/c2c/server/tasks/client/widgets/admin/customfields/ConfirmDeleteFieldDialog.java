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
package com.tasktop.c2c.server.tasks.client.widgets.admin.customfields;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.ICustomFieldsAdminView.Presenter;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
public class ConfirmDeleteFieldDialog extends DialogBox {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ConfirmDeleteFieldDialog> {
	}

	private static ConfirmDeleteFieldDialog instance;

	public static ConfirmDeleteFieldDialog getInstance(Presenter presenter, FieldDescriptor customField) {
		if (instance == null) {
			instance = new ConfirmDeleteFieldDialog();
		}
		instance.setCustomField(customField);
		instance.setPresenter(presenter);

		return instance;
	}

	// @UiField
	// protected Label fieldName;

	private FieldDescriptor customField;
	private Presenter presenter;

	private ConfirmDeleteFieldDialog() {
		super(false, true);
		setText("Confirm Delete");
		setWidget(uiBinder.createAndBindUi(this));
		setAnimationEnabled(true); // Why not?
		setGlassEnabled(true);
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (isShowing()) {
					center();
				}
			}
		});
	}

	private void setCustomField(FieldDescriptor customField) {
		// fieldName.setText(customField.getName());
		this.customField = customField;
	}

	private void setPresenter(Presenter presenter) {
		this.presenter = presenter;

	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent event) {
		hide();
	}

	@UiHandler("deleteButton")
	void onDelete(ClickEvent event) {
		presenter.deleteCustomField(customField);
		hide();
	}

}
