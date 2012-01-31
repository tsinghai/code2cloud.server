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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.profile.domain.internal.Profile;

public class ProfilePasswordValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Profile.class.isAssignableFrom(clazz);
	}

	/**
	 * 
	 * A password must be a string of characters that has:
	 * <ul>
	 * <li>length >= 8 characters</li>
	 * <li>at least 1 lowercase character</li>
	 * <li>at least 1 number character</li>
	 * <li>does not include the username</li>
	 * </ul>
	 */
	@Override
	public void validate(Object target, Errors errors) {
		Profile profile = (Profile) target;
		ValidationUtils.rejectIfEmpty(errors, "password", "field.required");
		if (profile.getPassword() != null && profile.getPassword().trim().length() > 0) {
			if (profile.getPassword().length() < 8) {
				errors.rejectValue("password", "password.tooShort");
			} else {
				final String password = profile.getPassword();
				final String lowerPassword = password.toLowerCase();
				if (profile.getUsername() != null && lowerPassword.contains(profile.getUsername().toLowerCase())) {
					errors.rejectValue("password", "password.cannotContainUsername");
				}

				// require 1 lowercase, 1 digit
				boolean hasLowercase = false;
				boolean hasDigits = false;

				for (int x = 0; x < password.length(); ++x) {
					char c = password.charAt(x);
					if (Character.isLetter(c)) {
						hasLowercase = hasLowercase || Character.isLowerCase(c);
					} else if (Character.isDigit(c)) {
						hasDigits = true;
					}
				}
				if (!hasLowercase) {
					errors.rejectValue("password", "password.requiresLowercase");
				}
				if (!hasDigits) {
					errors.rejectValue("password", "password.requiresDigit");
				}
			}
		}
	}

}
