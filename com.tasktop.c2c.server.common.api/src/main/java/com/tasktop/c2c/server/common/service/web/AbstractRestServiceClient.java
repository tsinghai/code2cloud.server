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

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;

/**
 * An abstract REST service client implementation
 * 
 */
public class AbstractRestServiceClient implements HasBaseUrl {

	protected RestTemplate template;
	private String baseUrl;

	public AbstractRestServiceClient() {
	}

	protected String computeUrl(String relativePath) {
		if (baseUrl == null) {
			throw new IllegalStateException();
		}
		return baseUrl + "/" + relativePath;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			throw new IllegalArgumentException();
		}
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		try {
			new URL(baseUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(baseUrl + " is not a valid URL", e);
		}
		this.baseUrl = baseUrl;
	}

	/**
	 * Convert the given exception to an AuthenticationException if possible. If the given exception wraps an
	 * authentication exception, it is thrown, otherwise this method does nothing and simply returns.
	 */
	protected void convertAuthenticationException(WrappedCheckedException e) throws AuthenticationException {
		Throwable cause = e.getCause();
		if (cause instanceof AuthenticationException) {
			throw (AuthenticationException) cause;
		}
	}

	/**
	 * Convert the given exception to an ValidationException if possible. If the given exception wraps an validation
	 * exception, it is thrown, otherwise this method does nothing and simply returns.
	 */
	protected final void convertValidationException(WrappedCheckedException e) throws ValidationException {
		if (e.getCause() instanceof ValidationException) {
			throw (ValidationException) e.getCause();
		}
	}

	/**
	 * Convert the given exception to an EntityNotFoundException if possible. If the given exception wraps an
	 * EntityNotFoundException exception, it is thrown, otherwise this method does nothing and simply returns.
	 */
	protected final void convertEntityNotFoundException(WrappedCheckedException e) throws EntityNotFoundException {
		if (e.getCause() instanceof EntityNotFoundException) {
			throw (EntityNotFoundException) e.getCause();
		}
	}

	/**
	 * Convert the given exception to an ConcurrentUpdateException if possible. If the given exception wraps an
	 * ConcurrentUpdateException exception, it is thrown, otherwise this method does nothing and simply returns.
	 */
	protected final void convertConcurrentUpdateException(WrappedCheckedException e) throws ConcurrentUpdateException {
		if (e.getCause() instanceof ConcurrentUpdateException) {
			throw (ConcurrentUpdateException) e.getCause();
		}
	}

	public void setRestTemplate(RestTemplate template) {
		this.template = template;
	}
}
