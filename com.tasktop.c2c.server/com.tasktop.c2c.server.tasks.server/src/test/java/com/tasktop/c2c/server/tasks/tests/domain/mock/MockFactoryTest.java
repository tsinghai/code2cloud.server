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
package com.tasktop.c2c.server.tasks.tests.domain.mock;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Task;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/applicationContext-test.xml" })
@Transactional
public class MockFactoryTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testProduct() {
		Product product = MockProductFactory.create(entityManager);
		entityManager.flush();
		assertTrue(entityManager.contains(product));
	}

	@Test
	public void testTask() {
		Product product = MockProductFactory.create(entityManager);
		Profile reporter = MockProfileFactory.create(entityManager);
		Task task = MockTaskFactory.create(entityManager, product, reporter);
		entityManager.flush();
		assertTrue(entityManager.contains(task));
	}

	@Test
	public void testProfile() {
		Profile profile = MockProfileFactory.create(entityManager);
		entityManager.flush();
		assertTrue(entityManager.contains(profile));
	}
}
