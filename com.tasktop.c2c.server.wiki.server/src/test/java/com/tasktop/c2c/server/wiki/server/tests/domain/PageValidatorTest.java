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
package com.tasktop.c2c.server.wiki.server.tests.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.common.tests.util.ValidationAssert;
import com.tasktop.c2c.server.internal.wiki.server.domain.validation.PageValidator;
import com.tasktop.c2c.server.wiki.domain.Page;


public class PageValidatorTest {
	private PageValidator pageValidator;
	private Page page;
	private Errors errors;

	@Before
	public void before() {
		pageValidator = new PageValidator();
		page = new Page();
		clearErrors();
	}

	private void clearErrors() {
		errors = new BeanPropertyBindingResult(page, "page");
	}

	@Test
	public void canValidatePages() {
		assertTrue(pageValidator.supports(Page.class));
		assertFalse(pageValidator.supports(com.tasktop.c2c.server.internal.wiki.server.domain.Page.class));
	}

	@Test
	public void validatesRequiredPath() {
		pageValidator.validate(page, errors);
		ValidationAssert.assertHaveValidationError(errors, "field.required.path");

		clearErrors();

		page.setPath("");
		pageValidator.validate(page, errors);
		ValidationAssert.assertHaveValidationError(errors, "field.required.path");
	}

	@Test
	public void validatesBadPathValue() {
		String[] badPaths = new String[] { " A", "A ", "A?" };
		for (String badPath : badPaths) {
			clearErrors();

			page.setPath(badPath);
			pageValidator.validate(page, errors);
			ValidationAssert.assertHaveValidationError(errors, "invalidValue.path", badPath);
		}
	}

	@Test
	public void validatesOkPathValue() {
		String[] okPaths = new String[] { "A", "A B", "A/B", "A_B" };
		page.setPath("A");
		for (String okPath : okPaths) {
			clearErrors();

			page.setPath(okPath);
			pageValidator.validate(page, errors);
			ValidationAssert.assertHaveNoValidationError(errors, "invalidValue.path");
		}
	}

	@Test
	public void validatesFailPathHashtagSeparator() {
		page.setPath("A-B");

		pageValidator.validate(page, errors);
		ValidationAssert.assertHaveValidationError(errors, "invalidValue.path", page.getPath());
	}
}
