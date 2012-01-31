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

import org.apache.commons.validator.UrlValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;

public class ScmRepositoryValidator implements Validator {

	private UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https", "git", "ssh" });

	@Override
	public boolean supports(Class<?> clazz) {
		return ScmRepository.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "url", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "type", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "scmLocation", "field.required");

		ScmRepository repo = (ScmRepository) target;

		if (!urlValidator.isValid(repo.getUrl())) {
			errors.reject("url.invalid");
		}
	}
}
