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
package com.tasktop.c2c.server.common.tests.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.common.service.ValidationException;


public class ValidationAssert {
	public static void assertHaveValidationError(Errors errors, String code, Object... args) {
		List<ObjectError> objectErrors = errors.getAllErrors();
		assertFalse(String.format("Expected error with code \"%s\"; there were no errors", code),
				objectErrors.isEmpty());

		String errorMessages = "";

		for (ObjectError error : objectErrors) {
			for (String errorCode : error.getCodes()) {
				if (errorCode.equals(code)) {
					if ((args == null || args.length == 0)
							&& (error.getArguments() == null || error.getArguments().length == 0)) {
						return;
					}
					if (args != null && error.getArguments() != null) {
						if (Arrays.equals(args, error.getArguments())) {
							return;
						}
					}
				}
			}
			if (errorMessages.length() > 0) {
				errorMessages += "\n";
			}
			errorMessages += String.format("\t%s", error);
		}
		fail(String.format("Expected error with code \"%s\" and args %s but got:\n%s", code, toMessageString(args),
				errorMessages));
	}

	private static Object toMessageString(Object[] args) {
		if (args == null || args.length == 0) {
			return "(none)";
		}
		String message = "";
		for (Object arg : args) {
			if (message.length() > 0) {
				message += ", ";
			}
			message += arg;
		}
		return "[" + message + "]";
	}

	public static void assertHaveValidationError(ValidationException e, String code, Object... args) {
		assertHaveValidationError(e.getErrors(), code, args);
	}

	public static void assertHaveNoValidationError(Errors errors, String code) {
		List<ObjectError> objectErrors = errors.getAllErrors();
		for (ObjectError error : objectErrors) {
			for (String errorCode : error.getCodes()) {
				if (errorCode.equals(code)) {
					fail(String.format("Expected no errors with code \"%s\" but got %s", code, error));
				}
			}
		}
	}
}
