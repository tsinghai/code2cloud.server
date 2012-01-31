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

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.ResponseErrorHandler;

import com.tasktop.c2c.server.common.service.HttpStatusCodeException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;

/**
 * Handles errors encountered during a REST service call. Used by RestTemplate.
 * 
 */
public class ServiceResponseErrorHandler implements ResponseErrorHandler {

	private HttpMessageConverter<Object> errorMessageConverter;

	private static final class ErrorResult {
		private Error error;

		public Error getError() {
			return error;
		}

		@SuppressWarnings("unused")
		public void setError(Error error) {
			this.error = error;
		}
	}

	public boolean hasError(ClientHttpResponse response) throws IOException {
		HttpStatus statusCode = response.getStatusCode();
		return (statusCode.series() == HttpStatus.Series.CLIENT_ERROR || statusCode.series() == HttpStatus.Series.SERVER_ERROR);
	}

	public void handleError(ClientHttpResponse response) throws IOException {
		ErrorResult errorResult;
		try {
			errorResult = (ErrorResult) errorMessageConverter.read(ErrorResult.class, response);
		} catch (HttpMessageNotReadableException e) {
			handleResponseStatus(response);
			throw e;
		}

		Error error = errorResult.getError();
		if (error == null) {
			throw new IllegalStateException("Can not get error contents");
		}

		Throwable e = error.getException();

		if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		}
		// Due to the declaration of this we can't throw checked exceptions here.
		throw new WrappedCheckedException(e);
	}

	private void handleResponseStatus(ClientHttpResponse response) throws IOException {
		if (response.getStatusCode() != null) {
			switch (response.getStatusCode()) {
			case FORBIDDEN:
			case UNAUTHORIZED:
				throw new BadCredentialsException("Bad Credentials");
			default:
				throw new HttpStatusCodeException(response.getStatusCode().value());
			}
		}
	}

	public HttpMessageConverter<Object> getErrorMessageConverter() {
		return errorMessageConverter;
	}

	public void setErrorMessageConverter(HttpMessageConverter<Object> errorMessageConverter) {
		this.errorMessageConverter = errorMessageConverter;
	}

}
