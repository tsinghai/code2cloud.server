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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;

public class CustomFieldEditor implements LeafValueEditor<CustomField> {

	private FieldDescriptor fieldDescriptor;
	private Widget widget;

	public CustomFieldEditor() {

	}

	@Override
	public void setValue(CustomField value) {
		fieldDescriptor = value.getFieldDescriptor();
		if (fieldDescriptor.getFieldType() == FieldType.LONG_TEXT) {
			TextArea valueWidget = new TextArea();
			valueWidget.setVisibleLines(4);
			valueWidget.setText(value.getValue());
			widget = valueWidget;
		} else {
			widget = createWidget(fieldDescriptor, value.getValue());
		}
	}

	@Override
	public CustomField getValue() {
		return new CustomField(fieldDescriptor, getFieldValue(widget));
	}

	private String getFieldValue(Widget value) {
		if (value instanceof TextBoxBase) {
			return ((TextBoxBase) value).getText();
		} else if (value instanceof Label) {
			return ((Label) value).getText();
		} else if (value instanceof DateBox) {
			Date date = ((DateBox) value).getValue();
			return date == null ? null : Long.toString(date.getTime());
		} else if (value instanceof ListBox) {
			ListBox listBox = (ListBox) value;
			String selectedValue = null;
			for (int x = 0; x < listBox.getItemCount(); ++x) {
				if (listBox.isItemSelected(x)) {
					if (selectedValue == null) {
						selectedValue = listBox.getValue(x);
					} else {
						selectedValue += ",";
						selectedValue += listBox.getValue(x);
					}
				}
			}
			return selectedValue;
		} else if (value instanceof CheckBox) {
			return ((CheckBox) value).getValue().toString();
		}
		throw new IllegalStateException();
	}

	private Widget createWidget(FieldDescriptor field, String fieldValue) {
		switch (field.getFieldType()) {
		case MULTI_SELECT:
		case SINGLE_SELECT:
			boolean isMultipleSelect = field.getFieldType() == FieldType.MULTI_SELECT;
			ListBox listBox = new ListBox(isMultipleSelect);
			int index = 0;
			Set<String> values = new HashSet<String>();
			if (fieldValue != null) {
				if (isMultipleSelect) {
					values.addAll(Arrays.asList(fieldValue.split("\\s*,\\s*")));
				} else {
					values.add(fieldValue);
				}
			}
			for (String value : field.getValueStrings()) {
				listBox.addItem(value, value);
				if (values.contains(value)) {
					listBox.setItemSelected(index, true);
				}
				++index;
			}
			return listBox;

		case TASK_REFERENCE: // FIXME: provide a better way
		case TEXT:
			TextBox textBox = new TextBox();
			textBox.setText(fieldValue);
			return textBox;

		case TIMESTAMP:
			DateBox dateBox = new DateBox();
			if (fieldValue != null && fieldValue.trim().length() > 0) {
				try {
					dateBox.setValue(new Date(Long.parseLong(fieldValue.trim())));
				} catch (NumberFormatException e) {
					// fall-back
					TextBox newTextBox = new TextBox();
					newTextBox.setText(fieldValue);
					return newTextBox;
				}
			}
			return dateBox;

		case CHECKBOX:
			CheckBox checkBox = new CheckBox();
			Boolean cbValue = fieldValue != null && Boolean.parseBoolean(fieldValue);
			checkBox.setValue(cbValue);
			return checkBox;

		}
		return new Label(fieldValue);
	}

	/**
	 * @return the widget
	 */
	public Widget getWidget() {
		return widget;
	}

	/**
	 * @return the fieldDescriptor
	 */
	public FieldDescriptor getFieldDescriptor() {
		return fieldDescriptor;
	}

}
