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
package com.tasktop.c2c.server.internal.deployment.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

/**
 * @author jhickey
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class DeploymentConfigurationValidatorTest extends AbstractValidatorTest<DeploymentConfiguration> {

	@Override
	protected DeploymentConfiguration createMock() {
		return new DeploymentConfiguration();
	}

	@Test
	public void testNameNull() {
		mock.setName(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required");
	}

	@Test
	public void testNameEmpty() {
		mock.setName("");
		validator.validate(mock, result);
		assertHaveValidationError("field.required");
	}

	@Test
	public void testNameWithSpaces() {
		mock.setName("Test One");
		validator.validate(mock, result);
		assertHaveValidationError("field.validDeploymentName");
	}

	@Test
	public void testNameWithSpecialChars() {
		// special characters should never appear in a deployment name.
		final String invalidChars = "~!@#$%^&*()+{}[]|\\;:\"'<>?,`.";
		final String namePrefix = "deployment";
		for (char invalidChar : invalidChars.toCharArray()) {
			clearErrors();
			mock.setName(namePrefix + invalidChar);
			validator.validate(mock, result);
			assertHaveValidationError("field.validDeploymentName");
		}
	}

	@Test
	public void testNameValid() {
		mock.setName("ThisIsValid_123");
		validator.validate(mock, result);
		assertTrue(result.getAllErrors().isEmpty());
	}

	@Test
	public void testNameValidDash() {
		mock.setName("ThisIsValid-123");
		validator.validate(mock, result);
		assertTrue(result.getAllErrors().isEmpty());
	}
}
