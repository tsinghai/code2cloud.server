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
package com.tasktop.c2c.server.profile.domain.validation;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.profile.domain.internal.Project;

public class ProjectValidator implements Validator {

	private Pattern namePattern = Pattern.compile("^[A-Z0-9][A-Z0-9\\._ -]+$", Pattern.CASE_INSENSITIVE);
	private int maxNameLength = 64; // Currently a requirement for task DB names

	@Override
	public boolean supports(Class<?> clazz) {
		return Project.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "name", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "description", "field.required");
		Project project = (Project) target;
		String name = project.getName();

		if (name != null && name.length() > 0) {
			// Don't show identifier validation error if name is blank
			ValidationUtils.rejectIfEmpty(errors, "identifier", "field.required");

			if (!namePattern.matcher(name).matches()) {
				errors.rejectValue("name", "field.validProjectName");
			}
			if (name.length() > maxNameLength) {
				errors.rejectValue("name", "field.tooLong");
			}
		}

		String description = project.getDescription();
		if (description != null && description.length() > 255) {
			errors.rejectValue("description", "field.tooLong");
		}

		ValidationUtils.rejectIfEmpty(errors, "public", "field.required");
	}

}
