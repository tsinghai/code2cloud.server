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
package com.tasktop.c2c.server.common.service.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.HttpStatusCodeException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.ServerRuntimeException;
import com.tasktop.c2c.server.common.service.ValidationException;

/**
 * Container for an error.
 * 
 */
public class Error {

	/** General Server Error code. */
	private static final String GENERAL_SERVER_EXCEPTION = "ServerException";

	private static Map<String, Class<? extends Throwable>> nameToExceptionType = new HashMap<String, Class<? extends Throwable>>();
	private static Set<Class<? extends Throwable>> exceptionTypes = new HashSet<Class<? extends Throwable>>();
	static {
		register(ValidationException.class);
		register(EntityNotFoundException.class);
		register(ConcurrentUpdateException.class);
		register(AccessDeniedException.class);
		register(AuthenticationException.class);
		register(BadCredentialsException.class);
		register(AuthenticationCredentialsNotFoundException.class);
		register(InsufficientAuthenticationException.class);
		register(InsufficientPermissionsException.class);
		register(UsernameNotFoundException.class);
		register(HttpStatusCodeException.class);
	}

	private String errorCode;
	private String message;
	private String detail;
	private ValidationErrors validationErrors;

	public Error() {

	}

	private static void register(Class<? extends Throwable> exceptionType) {
		if (exceptionType != ValidationException.class) {
			try {
				exceptionType.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(e);
			}
		}

		Class<? extends Throwable> previous = nameToExceptionType.put(exceptionType.getSimpleName(), exceptionType);
		if (previous != null) {
			throw new IllegalStateException();
		}
		exceptionTypes.add(exceptionType);
	}

	public Error(String message) {
		this.errorCode = GENERAL_SERVER_EXCEPTION;
		this.message = message;
	}

	/**
	 * Construct an error from an exception.
	 * 
	 * @param e
	 */
	public Error(Throwable e) {
		this(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
		if (exceptionTypes.contains(e.getClass())) {
			errorCode = e.getClass().getSimpleName();
		}
		StringWriter detailWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(detailWriter);
		e.printStackTrace(printWriter);
		printWriter.close();
		detail = detailWriter.toString();

	}

	/**
	 * Construct an error from a validation exception.
	 */
	public Error(ValidationException validationException, MessageSource messageSource) {
		this(validationException);
		validationErrors = new ValidationErrors();
		message = "";
		for (ObjectError error : validationException.getErrors().getAllErrors()) {
			String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : null;
			String defaultMessage;
			try {
				defaultMessage = messageSource.getMessage(error, getLocale());
			} catch (NoSuchMessageException e) {
				defaultMessage = error.getDefaultMessage();
				LoggerFactory.getLogger(Error.class).warn(e.getMessage(), e);
			}
			validationErrors.addError(error.getObjectName(), fieldName, error.getCode(), error.getArguments(),
					defaultMessage);
			if (message.length() > 0) {
				message += ", ";
			}
			message += defaultMessage;
		}
	}

	private Locale getLocale() {
		// FIXME
		return Locale.ENGLISH;
	}

	/**
	 * Construct an error from an authentication exception.
	 * 
	 * @param e
	 */
	public Error(org.springframework.security.core.AuthenticationException e) {
		this(e.getMessage());
		if (!exceptionTypes.contains(e.getClass())) {
			e = new BadCredentialsException(e.getMessage(), e);
		}
		if (exceptionTypes.contains(e.getClass())) {
			errorCode = e.getClass().getSimpleName();
		}
	}

	/**
	 * a message that describes the cause of the error
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * a code that identifies the kind of exception that occurred.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * A detailed message for diagnostic information. Normally this would not be presented to the user.
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * A detailed message for diagnostic information. Normally this would not be presented to the user.
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public void setValidationErrors(ValidationErrors validationErrors) {
		this.validationErrors = validationErrors;
	}

	/**
	 * Build the exception that this error represents.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Throwable getException() {
		Throwable e;
		Class<? extends Throwable> exceptionClass = nameToExceptionType.get(errorCode);
		if (exceptionClass != null) {
			if (exceptionClass == ValidationException.class) {
				e = new ValidationException(message, new ValidationErrorsBridge(validationErrors));
			} else {
				try {
					e = exceptionClass.getConstructor(String.class).newInstance(message);
				} catch (Throwable t) {
					throw new IllegalStateException(message, t);
				}
			}
		} else {
			e = new ServerRuntimeException(message);
		}
		return e;
	}
}
