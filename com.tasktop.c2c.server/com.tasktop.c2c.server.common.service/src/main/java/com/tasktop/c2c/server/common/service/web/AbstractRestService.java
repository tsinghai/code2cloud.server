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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Conventions;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.web.Error;

/**
 * Base class which wraps an existing service and exposing it over REST. Currently just provides the error handling.
 */
public abstract class AbstractRestService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected MessageSource messageSource;

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView handleValidationException(ValidationException ex, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}
		return createModelAndView("validationError", new Error(ex, messageSource));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ModelAndView handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}
		return createModelAndView("authenticationError", new Error(ex));
	}

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}
		return createModelAndView("entityNotFound", new Error(ex));
	}

	// XXX Remove this after task 1639 is done.
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ModelAndView handleAccessDenied(AccessDeniedException ex, HttpServletRequest request,
			HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug(ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}
		response.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", "Code2Cloud"));

		return createModelAndView("authenticationError", new Error(ex));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
		if (logger.isErrorEnabled()) {
			logger.error(ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}
		return createModelAndView("generalError", new Error(ex));
	}

	private ModelAndView createModelAndView(String viewName, Object value) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		String variableName = Conventions.getVariableName(value);
		modelAndView.addObject(variableName, value);
		return modelAndView;
	}

	protected void writeSimpleJsonObject(HttpServletResponse response, String property, Object value)
			throws IOException {
		String valueString;
		if (value instanceof Number) {
			valueString = value.toString();
		} else if (value instanceof String) {
			valueString = "\"" + (String) value + "\"";
		} else if (value instanceof Boolean) {
			valueString = (Boolean) value ? "true" : "false";
		} else {
			throw new IllegalArgumentException("Not handling value type: " + value.getClass());
		}

		response.getWriter().write("{ \"" + property + "\" : " + valueString + "}");
		response.setContentType("application/json");
	}
}
