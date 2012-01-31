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
package com.tasktop.c2c.server.profile.tests.domain.validation;

import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.validation.ProfilePasswordValidator;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;

public class ProfilePasswordValidatorTest extends AbstractValidatorTest<Profile> {

	@Before
	@Override
	public void before() {
		super.before();
		validator = new ProfilePasswordValidator();
	}

	@Override
	protected Profile createMock() {
		return MockProfileFactory.create(null);
	}

	@Test
	public void passwordRequired_Null() {
		mock.setPassword(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.password");
	}

	@Test
	public void passwordTooShort() {
		mock.setPassword("1234567");
		validator.validate(mock, result);
		assertHaveValidationError("password.tooShort");
	}

	@Test
	public void passwordRequiresDigit() {
		mock.setPassword("abcdefgh");
		validator.validate(mock, result);
		assertHaveValidationError("password.requiresDigit");
	}

	@Test
	public void passwordRequiresLowercase() {
		mock.setPassword("12345678");
		validator.validate(mock, result);
		assertHaveValidationError("password.requiresLowercase");
	}

	@Test
	public void passwordUnsafe_cannotContainUsername() {
		mock.setPassword(mock.getUsername() + "ABS");
		validator.validate(mock, result);
		assertHaveValidationError("password.cannotContainUsername");
	}
}
