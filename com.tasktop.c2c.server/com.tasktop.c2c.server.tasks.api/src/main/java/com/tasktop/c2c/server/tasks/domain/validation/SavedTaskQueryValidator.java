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

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public class SavedTaskQueryValidator implements Validator {

	private final Pattern namePattern = Pattern.compile("[\\w ]+");

	public boolean supports(Class<?> clazz) {
		return SavedTaskQuery.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "queryString", "field.required");

		SavedTaskQuery query = (SavedTaskQuery) target;
		try {
			Criteria c = query.getQueryCriteria();
		} catch (Exception e) {
			errors.rejectValue("queryString", "invalid");
		}

		String name = query.getName();
		if (name != null && name.length() > 0) {
			if (!namePattern.matcher(name).matches()) {
				errors.rejectValue("name", "field.validQueryName");
			}
		}

	}

}
