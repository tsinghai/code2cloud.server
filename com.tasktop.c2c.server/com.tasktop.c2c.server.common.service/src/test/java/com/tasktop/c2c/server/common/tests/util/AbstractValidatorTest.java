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

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

public abstract class AbstractValidatorTest<T> {

	protected static final String[] BLANK_VALUES = { null, "", "   \n\n" };
	@Autowired
	protected Validator validator;
	protected BeanPropertyBindingResult result;
	protected T mock;

	public AbstractValidatorTest() {
	}

	@Before
	public void before() {
		mock = createMock();
		clearErrors();
	}

	protected void clearErrors() {
		result = new BeanPropertyBindingResult(mock, mock.getClass().getSimpleName().toLowerCase());
	}

	protected abstract T createMock();

	protected void assertHaveValidationError(String code, Object... args) {
		ValidationAssert.assertHaveValidationError(result, code, args);
	}

}
