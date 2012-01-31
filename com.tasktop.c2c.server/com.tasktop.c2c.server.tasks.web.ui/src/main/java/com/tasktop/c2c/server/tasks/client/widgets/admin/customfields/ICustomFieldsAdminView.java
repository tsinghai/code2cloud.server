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

import java.util.List;


import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

public interface ICustomFieldsAdminView extends IsWidget {

	void setPresenterAndUpdateDisplay(Presenter presenter);

	public static interface Presenter {
		List<FieldDescriptor> getCustomFields();

		void createCustomField(FieldDescriptor newField);

		void updateCustomField(FieldDescriptor newField);

		void edit(FieldDescriptor selectedObject);

		void newCustomField();

		void cancelNew();

		void cancelEdit();

		void deleteCustomField(FieldDescriptor customField);

		List<CustomFieldValue> getNewCustomFieldsValues();
	}

	void editField(FieldDescriptor field);

	void newField(FieldDescriptor newField);
}
