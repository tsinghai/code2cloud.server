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

import java.util.Arrays;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueListBox;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.ICustomFieldsAdminView.Presenter;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class NewCustomFieldView extends Composite implements Editor<FieldDescriptor> {
	interface Binder extends UiBinder<HTMLPanel, NewCustomFieldView> {
	}

	interface Driver extends SimpleBeanEditorDriver<FieldDescriptor, NewCustomFieldView> {
	}

	private static Binder binder = GWT.create(Binder.class);
	private static Driver driver = GWT.create(Driver.class);
	private static NewCustomFieldView instance;

	@UiField
	protected TextBox name;
	@UiField
	protected TextBox description;
	@UiField(provided = true)
	protected ValueListBox<FieldType> fieldType = new ValueListBox<FieldType>(FieldTypeRender.getInstance());
	@UiField
	protected CheckBox availableForNewTasks;
	@UiField
	protected CustomFieldValuesEditor values;
	@UiField
	protected Element fieldValuesContainer;

	private Presenter presenter;

	public static NewCustomFieldView getInstance() {
		if (instance == null) {
			instance = new NewCustomFieldView();
		}
		return instance;
	}

	private NewCustomFieldView() {
		initWidget(binder.createAndBindUi(this));
		driver.initialize(this);
		fieldType.addValueChangeHandler(new ValueChangeHandler<FieldType>() {

			@Override
			public void onValueChange(ValueChangeEvent<FieldType> event) {
				onFieldTypeChange(event.getValue());

			}
		});
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setValue(FieldDescriptor field) {
		driver.edit(field);
		onFieldTypeChange(field.getFieldType());
		fieldType.setAcceptableValues(Arrays.asList(FieldType.values()));
	}

	private void onFieldTypeChange(FieldType value) {
		boolean showValues;
		switch (value) {
		case MULTI_SELECT:
		case SINGLE_SELECT:
			values.setValue(presenter.getNewCustomFieldsValues());
			showValues = true;
			break;
		default:
			showValues = false;
		}
		UIObject.setVisible(fieldValuesContainer, showValues);
	}

	@UiHandler("cancelButton")
	protected void onCancel(ClickEvent ce) {
		presenter.cancelNew();
	}

	@UiHandler("saveButton")
	protected void onSave(ClickEvent ce) {
		FieldDescriptor newField = driver.flush();
		presenter.createCustomField(newField);
	}

}
