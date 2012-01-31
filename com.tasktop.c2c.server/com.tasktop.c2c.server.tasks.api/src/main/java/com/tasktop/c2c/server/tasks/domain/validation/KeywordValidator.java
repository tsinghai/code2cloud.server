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

import com.tasktop.c2c.server.tasks.domain.Keyword;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class KeywordValidator implements Validator {
	public boolean supports(Class<?> requestedClass) {
		return Keyword.class.isAssignableFrom(requestedClass);
	}

	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required");

		String name = ((Keyword) target).getName();
		if (name != null && name.length() > 0) {
			if (!Pattern.matches("[A-Za-z0-9]+", name)) {
				errors.rejectValue("name", "keyword.invalidCharacters");
			}
		}
	}
}
