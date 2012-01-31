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
package com.tasktop.c2c.server.common.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.web.Error;


public class ErrorTest {

	@Test
	public void testValidationException() {
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(this, "foo");
		errors.addError(new ObjectError("foo", "message1"));
		errors.addError(new ObjectError("foo", "message2"));
		ValidationException originalException = new ValidationException(errors);

		Error error = new Error(originalException);

		Throwable exception = error.getException();

		assertSame(originalException.getClass(), exception.getClass());
		assertEquals(originalException.getMessage(), exception.getMessage());
	}

	@Test
	public void testAuthenticationException() {
		AuthenticationException originalException = new AuthenticationException("Authentication required");

		Error error = new Error(originalException);

		Throwable exception = error.getException();

		assertSame(originalException.getClass(), exception.getClass());
		assertEquals(originalException.getMessage(), exception.getMessage());
	}

	@Test
	public void testAuthenticationExceptionNoMessage() {
		AuthenticationException originalException = new AuthenticationException();

		Error error = new Error(originalException);

		Throwable exception = error.getException();

		assertSame(originalException.getClass(), exception.getClass());
		assertNotNull(exception.getMessage());
	}

	@Test
	public void testEntityNotFoundException() {
		EntityNotFoundException originalException = new EntityNotFoundException("test");

		Error error = new Error(originalException);

		Throwable exception = error.getException();

		assertSame(originalException.getClass(), exception.getClass());
		assertEquals(originalException.getMessage(), exception.getMessage());
	}

	@Test
	public void testIllegalStateException() {
		IllegalStateException originalException = new IllegalStateException("test");

		Error error = new Error(originalException);

		Throwable exception = error.getException();

		assertNotSame(originalException.getClass(), exception.getClass());
		assertEquals(originalException.getMessage(), exception.getMessage());
	}
}
