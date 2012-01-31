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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.tasks.domain.WorkLog;

public class WorkLogValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return WorkLog.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "profile", "field.required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "hoursWorked", "field.required");
		WorkLog log = (WorkLog) target;
		if (log.getHoursWorked() != null && log.getHoursWorked().signum() != 1) {
			errors.rejectValue("hoursWorked", "notPositive");
		}
	}

}
