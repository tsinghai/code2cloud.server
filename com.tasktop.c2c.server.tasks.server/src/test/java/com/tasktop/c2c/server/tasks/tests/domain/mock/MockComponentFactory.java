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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;


public class MockComponentFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static Component create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Component> create(EntityManager entityManager, int numComponents) {

		Product newProd = MockProductFactory.create(entityManager, 1, 0).get(0);

		// Put this into the persistence context.
		entityManager.persist(newProd);

		return createWithProduct(entityManager, numComponents, newProd);
	}

	public static List<Component> createWithProduct(EntityManager entityManager, int count, Product parentProd) {
		List<Component> mocks = new ArrayList<Component>(count);
		for (int x = 0; x < count; ++x) {
			Component mock = populate(new Component(), entityManager, parentProd);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static Component populate(Component component, EntityManager entityManager, Product parentProd) {
		int index = created.incrementAndGet();

		component.setName("Component" + index);
		component.setDescription("a description");
		component.setProduct(parentProd);

		component.setInitialOwner(entityManager.find(Profile.class, Integer.valueOf(1)));

		return component;
	}
}
