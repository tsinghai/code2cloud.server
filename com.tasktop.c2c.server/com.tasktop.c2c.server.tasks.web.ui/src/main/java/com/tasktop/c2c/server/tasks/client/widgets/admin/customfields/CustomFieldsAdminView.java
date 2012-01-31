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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

public class CustomFieldsAdminView extends Composite implements ICustomFieldsAdminView {

	interface Binder extends UiBinder<HTMLPanel, CustomFieldsAdminView> {
	}

	private static Binder ourUiBinder = GWT.create(Binder.class);

	private static CustomFieldsAdminView instance;

	public static CustomFieldsAdminView getInstance() {
		if (instance == null) {
			instance = new CustomFieldsAdminView();
		}
		return instance;
	}

	@UiField
	protected SimplePanel contentContainer;
	@UiField(provided = true)
	protected CustomFieldsMenu menu = CustomFieldsMenu.getInstance();

	private CustomFieldEditView editView = CustomFieldEditView.getInstance();
	private NewCustomFieldView newView = NewCustomFieldView.getInstance();

	private CustomFieldsAdminView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void setPresenterAndUpdateDisplay(Presenter presenter) {
		menu.setPresenterAndUpdateDisplay(presenter);
		editView.setPresenter(presenter);
		newView.setPresenter(presenter);
		contentContainer.clear();
	}

	@Override
	public void editField(FieldDescriptor selectedObject) {
		contentContainer.setWidget(editView);
		editView.setValue(selectedObject);
	}

	@Override
	public void newField(FieldDescriptor newField) {
		contentContainer.setWidget(newView);
		newView.setValue(newField);
	}

}
