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

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class ProjectValidatorTest extends AbstractValidatorTest<Project> {

	@Override
	protected Project createMock() {
		return MockProjectFactory.create(null);
	}

	@Test
	public void testNameNull() {
		mock.setName(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.project.name");
	}

	@Test
	public void testNameEmpty() {
		mock.setName("");
		validator.validate(mock, result);
		assertHaveValidationError("field.required.project.name");
	}

	@Test
	public void testNameOk() {
		mock.setName("foo.bar baz");
		mock.computeIdentifier();
		validator.validate(mock, result);
		assertTrue(result.getAllErrors().isEmpty());
	}

	@Test
	public void testNameInvalid() {
		mock.setName("foo.bar baz!");
		mock.computeIdentifier();
		validator.validate(mock, result);
		assertHaveValidationError("field.validProjectName");
	}

	@Test
	public void testNameInvalidCharacters() {

		final String namePrefix = "foo.bar baz";

		// sanity: verify that our base name is ok
		mock.setName(namePrefix);
		mock.computeIdentifier();
		validator.validate(mock, result);
		assertTrue(result.getAllErrors().isEmpty());

		// these characters should never appear in an application name.
		final String invalidChars = "~!@#$%^&*()+{}[]|\\;:\"'<>?,`";

		for (char invalidChar : invalidChars.toCharArray()) {
			clearErrors();
			mock.setName(namePrefix + invalidChar);
			mock.computeIdentifier();

			validator.validate(mock, result);
			assertHaveValidationError("field.validProjectName");
		}
	}

	@Test
	public void publicNull() {
		mock.setPublic(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.public");
	}

	@Test
	public void descriptionTooLong() {
		mock.setDescription(RandomStringUtils.random(256));
		validator.validate(mock, result);
		assertHaveValidationError("field.tooLong.description");
	}
}
