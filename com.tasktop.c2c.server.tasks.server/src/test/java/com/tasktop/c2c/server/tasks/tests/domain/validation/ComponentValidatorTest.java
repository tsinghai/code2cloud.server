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
package com.tasktop.c2c.server.tasks.tests.domain.validation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConversionContext;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConverter;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockComponentFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class ComponentValidatorTest extends AbstractValidatorTest<Component> {

	@PersistenceContext(unitName = "tasksDomain")
	private EntityManager entityManager;

	@Autowired
	private DomainConverter domainConverter;

	@Override
	protected Component createMock() {
		return (Component) domainConverter.convert(MockComponentFactory.create(entityManager),
				new DomainConversionContext(entityManager));
	}

	@Test
	public void testMockIsValid() {
		validator.validate(mock, result);
		Assert.assertFalse(result.toString(), result.hasErrors());
	}

	@Test
	public void nameRequired() {
		for (String value : BLANK_VALUES) {
			mock.setName(value);
			validator.validate(mock, result);
			assertHaveValidationError("field.required.name");
		}
	}

	@Test
	public void descriptionRequired() {
		for (String value : BLANK_VALUES) {
			mock.setDescription(value);
			validator.validate(mock, result);
			assertHaveValidationError("field.required.description");
		}
	}
}
