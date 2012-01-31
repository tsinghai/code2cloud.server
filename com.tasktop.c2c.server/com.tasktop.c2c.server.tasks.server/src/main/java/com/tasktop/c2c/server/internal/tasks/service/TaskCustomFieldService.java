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
package com.tasktop.c2c.server.internal.tasks.service;

import java.util.List;
import java.util.Map;


import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

public interface TaskCustomFieldService {

	List<CustomFieldValue> getFieldValues(FieldDescriptor descriptor) throws EntityNotFoundException;

	FieldDescriptor createCustomField(FieldDescriptor descriptor) throws ValidationException;

	void removeCustomField(FieldDescriptor descriptor) throws EntityNotFoundException;

	List<FieldDescriptor> getCustomFields();

	Map<String, Object> retrieveTaskCustomFields(Integer taskId) throws EntityNotFoundException;

	void updateTaskCustomFields(Integer taskId, Map<String, Object> fields) throws EntityNotFoundException;

	String getColumnName(FieldDescriptor descriptor);

	void addNewValue(FieldDescriptor descriptor, CustomFieldValue newValue);

	void udpateValue(FieldDescriptor descriptor, CustomFieldValue value);

	FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException, EntityNotFoundException;

}
