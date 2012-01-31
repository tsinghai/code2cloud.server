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
package com.tasktop.c2c.server.tasks.domain.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

public class FieldDescriptorValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return FieldDescriptor.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "field.required");

		FieldDescriptor customField = (FieldDescriptor) target;

		if (customField.getValues() != null) {
			int i = 0;
			Set<String> values = new HashSet<String>();
			for (CustomFieldValue value : new ArrayList<CustomFieldValue>(customField.getValues())) {
				i++;
				if (value.getValue() == null || value.getValue().isEmpty()) {
					errors.rejectValue("values[" + i + "].value", "field.required");
				}
				if (value.getSortkey() == null) {
					errors.rejectValue("values[" + i + "].sortkey", "field.required");
				}
				if (values.contains(value.getValue())) {
					errors.rejectValue("values", "name.unique");
				}
				values.add(value.getValue());

			}
		}
	}

}
