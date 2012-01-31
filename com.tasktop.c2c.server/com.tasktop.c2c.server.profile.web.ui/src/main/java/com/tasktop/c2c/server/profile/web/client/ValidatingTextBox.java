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
package com.tasktop.c2c.server.profile.web.client;

import java.util.Map;


import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;

public class ValidatingTextBox extends Composite implements IsEditor<ValueBoxEditor<String>> {

	private FlowPanel flowPanel = new FlowPanel();
	private TextBox textBox = new TextBox();
	private Label label = new Label();

	private Map<String, String> errorMap;
	private String fieldName;
	private AbstractDomainObject domainObject;

	public ValidatingTextBox() {
		flowPanel.add(textBox);
		flowPanel.add(label);

		// Prepare our label's style settings. It can stay red forever since it'll be made visible and invisible.
		label.setVisible(false);
		label.setStylePrimaryName("errorLabel");

		// Set the border of the panel to be hidden for now.
		flowPanel.addStyleName("errorLabelWrapperHidden");

		initWidget(flowPanel);
	}

	public Map<String, String> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<String, String> errorMap) {
		this.errorMap = errorMap;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public AbstractDomainObject getDomainObject() {
		return domainObject;
	}

	public void setDomainObject(AbstractDomainObject domainObject) {
		this.domainObject = domainObject;
	}

	public String getText() {
		return textBox.getText();
	}

	public void setText(String newText) {
		this.textBox.setText(newText);
		redraw();
	}

	public void redraw() {
		// the setText() method is used to trigger a re-render, so check now to see if we should render an error.
		if (ValidationUtils.hasError(domainObject, fieldName, errorMap)) {
			// Make our overall panel red
			flowPanel.removeStyleName("errorLabelWrapperHidden");
			flowPanel.addStyleName("errorLabelWrapper");

			// Set the label's text, and make it visible.
			label.setText(ValidationUtils.getErrorMessage(domainObject, fieldName, errorMap));
			label.setVisible(true);

		} else {

			// hide our label
			label.setVisible(false);
			flowPanel.removeStyleName("errorLabelWrapper");
			flowPanel.addStyleName("errorLabelWrapperHidden");
		}
	}

	@Override
	public ValueBoxEditor<String> asEditor() {
		return textBox.asEditor();
	}
}
