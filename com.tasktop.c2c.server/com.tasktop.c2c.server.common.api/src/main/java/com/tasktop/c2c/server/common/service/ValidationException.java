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
package com.tasktop.c2c.server.common.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Errors errors;

	public ValidationException(Errors errors) {
		super(computeMessage(errors));
		this.errors = errors;
	}

	public ValidationException(Errors errors, MessageSource messageSource) {
		super(computeMessage(errors, messageSource));
		this.errors = errors;
	}

	public ValidationException(String message, Errors errors) {
		super(message);
		this.errors = errors;
	}

	public Errors getErrors() {
		return errors;
	}

	private static String computeMessage(Errors errors) {
		StringBuffer message = new StringBuffer();
		for (ObjectError error : errors.getAllErrors()) {
			if (message.length() > 0) {
				message.append(", ");
			}
			message.append(error.getCode());
		}
		return message.toString();
	}

	private static String computeMessage(Errors errors, MessageSource messageSource) {
		StringBuffer message = new StringBuffer();
		for (ObjectError error : errors.getAllErrors()) {
			if (message.length() > 0) {
				message.append(", ");
			}
			message.append(messageSource.getMessage(error, Locale.ENGLISH));
		}
		return message.toString();
	}

}
