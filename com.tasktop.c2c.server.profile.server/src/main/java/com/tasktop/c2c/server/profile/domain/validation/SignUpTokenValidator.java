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

import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;


public class SignUpTokenValidator implements Validator {

	private Pattern emailPattern = Pattern.compile("^[A-Z0-9\\._%+-]+@[A-Z0-9\\.-]+\\.[A-Z]{2,4}$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public boolean supports(Class<?> clazz) {
		return SignUpToken.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SignUpToken token = (SignUpToken) target;
		ValidationUtils.rejectIfEmpty(errors, "firstname", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "lastname", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "email", "field.required");

		String email = token.getEmail();
		if (email != null && email.length() > 0) {
			if (!emailPattern.matcher(email).matches()) {
				errors.rejectValue("email", "field.validEmail");
			}
		}
	}
}
