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
package com.tasktop.c2c.server.internal.tasks.tests.domain.validation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProductFactory;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class ProductValidatorTest extends AbstractValidatorTest<Product> {
	@PersistenceContext(unitName = "tasksDomain")
	private EntityManager entityManager;

	@Autowired
	private Validator internalValidator;

	@Override
	protected Product createMock() {
		com.tasktop.c2c.server.internal.tasks.domain.Product mockProduct = MockProductFactory
				.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		return (Product) mockProduct;
	}

	@Test
	public void defaultMilestoneInMilestones() {
		Milestone m = new Milestone();
		m.setValue("bogus value adfasdfadfadfadf");

		mock.setDefaultmilestone(m.getValue());
		internalValidator.validate(mock, result);
		assertHaveValidationError("defaultMilestone.notInAssociatedMilestones");
	}
}
