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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class ProfileValidatorTest extends AbstractValidatorTest<Profile> {

	@Override
	protected Profile createMock() {
		return MockProfileFactory.create(null);
	}

	@Test
	public void usernameRequiredNull() {
		mock.setUsername(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.username");
	}

	@Test
	public void usernameRequiredEmpty() {
		mock.setUsername("");
		validator.validate(mock, result);
		assertHaveValidationError("field.required.username");
	}

	@Test
	public void usernameNotReserved() {
		mock.setUsername("NONE_PROVIDED");
		validator.validate(mock, result);
		assertHaveValidationError("profile.usernameUnique");
	}

	@Test
	public void emailRequiredNull() {
		mock.setEmail(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.email");
	}

	@Test
	public void emailRequiredValidEmail() {
		mock.setEmail("foo@");
		validator.validate(mock, result);
		assertHaveValidationError("field.validEmail.email");
	}

	@Test
	public void firstNameRequiredNull() {
		mock.setFirstName(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.firstName");
	}

	@Test
	public void lastNameRequiredNull() {
		mock.setLastName(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.lastName");
	}

}
