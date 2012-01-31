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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.profile.domain.internal.Profile;


public class ProfileValidator implements Validator {

	private Pattern emailPattern = Pattern.compile("^[A-Z0-9\\._%+-]+@[A-Z0-9\\.-]+\\.[A-Z]{2,4}$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * IMPORTANT: cannot allow characters (such as ':') that are used in HTTP authentication
	 */
	private Pattern usernamePattern = Pattern.compile("^[A-Z0-9\\._%+-]+$", Pattern.CASE_INSENSITIVE);

	private Set<String> reservedNames = new HashSet<String>();
	{

		// NONE_PROVIDED is used by spring, "anonymous" is used by Git, SYSTEM is used for system jobs
		reservedNames.add("NONE_PROVIDED".toLowerCase());
		reservedNames.add("anonymous".toLowerCase());
		reservedNames.add("SYSTEM".toLowerCase());
		// disallow these to avoid confusion
		reservedNames.add("none".toLowerCase());
		reservedNames.add("void".toLowerCase());
		reservedNames.add("null".toLowerCase());
		reservedNames.add("unknown".toLowerCase());
		// santity: avoid common system (unix) usernames
		reservedNames.add("root".toLowerCase());
		reservedNames.add("lpr".toLowerCase());
		reservedNames.add("lp".toLowerCase());
		reservedNames.add("administrator".toLowerCase());
		reservedNames.add("daemon".toLowerCase());
		reservedNames.add("sys".toLowerCase());
		reservedNames.add("sync".toLowerCase());
		reservedNames.add("man".toLowerCase());
		reservedNames.add("mail".toLowerCase());
		reservedNames.add("news".toLowerCase());
		reservedNames.add("uucp".toLowerCase());
		reservedNames.add("proxy".toLowerCase());
		reservedNames.add("backup".toLowerCase());
		reservedNames.add("list".toLowerCase());
		reservedNames.add("irc".toLowerCase());
		reservedNames.add("nobody".toLowerCase());
		reservedNames.add("syslog".toLowerCase());
		reservedNames.add("http".toLowerCase());
		reservedNames.add("httpd".toLowerCase());
		reservedNames.add("wwww".toLowerCase());
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Profile.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Profile profile = (Profile) target;
		ValidationUtils.rejectIfEmpty(errors, "username", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "firstName", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "lastName", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "email", "field.required");

		// we don't validate the password here, since it might not have changed and
		// we can't validated it after it's been hashed.

		String email = profile.getEmail();
		if (email != null && email.length() > 0) {
			if (!emailPattern.matcher(email).matches()) {
				errors.rejectValue("email", "field.validEmail");
			}
		}
		if (profile.getUsername() != null && profile.getUsername().length() > 0) {
			if (!usernamePattern.matcher(profile.getUsername()).matches()
					&& !emailPattern.matcher(profile.getUsername()).matches()) {
				errors.rejectValue("username", "field.validUsername", new Object[] { profile.getUsername() },
						"bad username");
			}
		}
		// verify that reserved usernames are not used.
		if (isReservedUsername(profile.getUsername())) {
			// we don't tell the user that the name is reserved, we just tell them
			// that the name is in use
			errors.rejectValue("username", "profile.usernameUnique");
		}
	}

	protected boolean isReservedUsername(String name) {
		return name != null && reservedNames.contains(name.toLowerCase());
	}
}
