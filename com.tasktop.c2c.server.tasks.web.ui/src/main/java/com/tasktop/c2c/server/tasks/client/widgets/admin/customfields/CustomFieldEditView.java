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
import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueLabel;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.ICustomFieldsAdminView.Presenter;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CustomFieldEditView extends Composite implements Editor<FieldDescriptor> {
	interface Binder extends UiBinder<HTMLPanel, CustomFieldEditView> {
	}

	interface Driver extends SimpleBeanEditorDriver<FieldDescriptor, CustomFieldEditView> {
	}

	private static Binder binder = GWT.create(Binder.class);
	private static Driver driver = GWT.create(Driver.class);
	private static CustomFieldEditView instance;

	@UiField
	protected Label name;
	@UiField
	protected TextBox description;
	@UiField(provided = true)
	protected ValueLabel<FieldType> fieldType = new ValueLabel<FieldType>(FieldTypeRender.getInstance());
	@UiField
	protected CheckBox availableForNewTasks;
	@UiField
	protected CheckBox obsolete;
	@UiField
	protected CustomFieldValuesEditor values;
	@UiField
	protected Element fieldValuesContainer;

	private Presenter presenter;
	private FieldDescriptor customField;

	public static CustomFieldEditView getInstance() {
		if (instance == null) {
			instance = new CustomFieldEditView();
		}
		return instance;
	}

	private CustomFieldEditView() {
		initWidget(binder.createAndBindUi(this));
		driver.initialize(this);

	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setValue(FieldDescriptor field) {
		this.customField = field;
		onFieldTypeChange(field.getFieldType());
		driver.edit(field);
	}

	private void onFieldTypeChange(FieldType value) {
		boolean showValues;
		switch (value) {
		case MULTI_SELECT:
		case SINGLE_SELECT:
			showValues = true;
			break;
		default:
			showValues = false;
		}
		UIObject.setVisible(fieldValuesContainer, showValues);
	}

	@UiHandler("cancelButton")
	protected void onCancel(ClickEvent ce) {
		presenter.cancelEdit();
	}

	@UiHandler("saveButton")
	protected void onSave(ClickEvent ce) {
		FieldDescriptor field = driver.flush();
		presenter.updateCustomField(field);
	}

	@UiHandler("deleteButton")
	protected void onDelete(ClickEvent ce) {
		ConfirmDeleteFieldDialog.getInstance(presenter, customField).center();
	}

}
