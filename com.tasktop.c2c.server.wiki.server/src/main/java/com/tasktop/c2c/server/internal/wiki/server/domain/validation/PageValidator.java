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
package com.tasktop.c2c.server.internal.wiki.server.domain.validation;

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.wiki.domain.Page;


public class PageValidator implements Validator {
	// NOTE: CAREFUL careful about adding allowable characters: in particular Path.HASHTAG_DELIMITER
	private static Pattern pathPattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9 /_]*");

	@Override
	public boolean supports(Class<?> clazz) {
		return Page.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Page page = (Page) target;

		ValidationUtils.rejectIfEmpty(errors, "path", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "content", "field.required");

		if (page.getPath() != null && page.getPath().length() > 0) {
			if (!pathPattern.matcher(page.getPath()).matches() || !page.getPath().trim().equals(page.getPath())) {
				errors.rejectValue("path", "invalidValue", new Object[] { page.getPath() }, "bad path");
			}
		}
	}

}
