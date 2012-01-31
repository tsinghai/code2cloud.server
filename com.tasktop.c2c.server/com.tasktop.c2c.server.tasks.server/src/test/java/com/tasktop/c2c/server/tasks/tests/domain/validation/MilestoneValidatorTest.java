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
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockMilestoneFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class MilestoneValidatorTest extends AbstractValidatorTest<Milestone> {

	@PersistenceContext(unitName = "tasksDomain")
	private EntityManager entityManager;

	@Autowired
	private DomainConverter domainConverter;

	@Override
	protected Milestone createMock() {
		return (Milestone) domainConverter.convert(MockMilestoneFactory.create(entityManager),
				new DomainConversionContext(entityManager));
	}

	@Test
	public void testMockIsValid() {
		validator.validate(mock, result);
		Assert.assertFalse(result.toString(), result.hasErrors());
	}

	@Test
	public void valueRequired() {
		for (String value : BLANK_VALUES) {
			mock.setValue(value);
			validator.validate(mock, result);
			assertHaveValidationError("field.required.value");
		}
	}

	@Test
	public void testSortkeyNull() {
		mock.setSortkey(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.sortkey");
	}

}
