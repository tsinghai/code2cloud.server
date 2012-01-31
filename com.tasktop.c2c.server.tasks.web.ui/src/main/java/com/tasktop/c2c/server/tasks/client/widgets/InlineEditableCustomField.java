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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class InlineEditableCustomField extends Composite {

	interface Binder extends UiBinder<Widget, InlineEditableCustomField> {

	}

	private static Binder binder = GWT.create(Binder.class);

	@UiField
	protected Label name;
	@UiField
	protected Anchor editTriggerAnchor;
	@UiField
	protected HTML readOnlyHtml;
	@UiField
	protected SimplePanel editingDiv;

	public InlineEditableCustomField(CustomFieldEditor editor) {
		initWidget(binder.createAndBindUi(this));

		FieldDescriptor fieldDescriptor = editor.getFieldDescriptor();

		name.setText(fieldDescriptor.getDescription());
		editingDiv.setWidget(editor.getWidget());

	}

	/**
	 * @return
	 */
	public HTML getReadOnlyField() {
		return readOnlyHtml;
	}

	/**
	 * @return
	 */
	public Anchor getEditFieldAnchor() {
		return editTriggerAnchor;
	}
}
