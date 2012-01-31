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
package com.tasktop.c2c.server.profile.web.client;

import java.util.Map;

import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;

/**
 * Centralize our handling of validation so that it's more easily reusable and consistent, and less prone to
 * reimplementation errors. I'd prefer to do this in an inheritance-based way, however it's not possible to due to GWT's
 * class structure.
 */
public class ValidationUtils {

	public static String calculateKey(Object key, String fieldName) {
		AbstractDomainObject domainObj = (AbstractDomainObject) key;

		// Class.getSimpleName isn't implemented in GWT, so we basically have to re-roll a quick version of it here - to
		// do that, we just find the last period and take the substring after that. That makes e.g.
		// "class com.tasktop.foo.Bar" return "Bar".
		String simpleName = domainObj.getClass().toString();
		simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);

		return "field.required." + fieldName + "|" + simpleName + "|" + domainObj.getId();
	}

	public static boolean hasError(Object key, String fieldName, Map<String, String> errorMap) {
		// If there's no key yet, then there can't be an error yet since we're initializing.
		if (key == null) {
			return false;
		}

		// Figure out what our key is, and use it to do our map lookup.
		String mapKey = ValidationUtils.calculateKey(key, fieldName);

		return errorMap.containsKey(mapKey);
	}

	public static String getErrorMessage(Object key, String fieldName, Map<String, String> errorMap) {
		// Figure out what our key is, and use it to do our map lookup.
		String mapKey = ValidationUtils.calculateKey(key, fieldName);

		return errorMap.get(mapKey);
	}

	public static boolean isCustomErrorMessage(String errorMessage) {
		// If the string's not null and contains a |, it's a custom error message.
		return (errorMessage != null) && (errorMessage.contains("|"));
	}

	public static void addErrorToMap(String customErrorMessage, Map<String, String> errorMap) {
		// This is one of our magic strings - push it into our error map.
		String errMsg = customErrorMessage.substring(0, customErrorMessage.indexOf('|'));
		// Skip the first | itself, and add the rest of the string
		String errMsgKey = customErrorMessage.substring(customErrorMessage.indexOf('|') + 1);

		// Put our message into the error map, keyed by the combination of classname, ID and field. The
		// ValidatingTextInputCell will be able to use that to pull out any pertinent error messages.
		errorMap.put(errMsgKey, errMsg);
	}

	private final static String EMAIL_VALIDATION_REGEX = ".+@.+\\.[a-z]+";

	public static boolean isValidEmail(String email) {
		return email != null && email.matches(EMAIL_VALIDATION_REGEX);
	}

}
