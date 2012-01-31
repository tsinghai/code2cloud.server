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

import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Product;


public class MockMilestoneFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static Milestone create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Milestone> create(EntityManager entityManager, int numMilestones) {

		Product newProd = MockProductFactory.create(entityManager, 1, 0).get(0);

		// Put this into the persistence context.
		entityManager.persist(newProd);

		return createWithProduct(entityManager, numMilestones, newProd);
	}

	public static List<Milestone> createWithProduct(EntityManager entityManager, int count, Product parentProd) {
		List<Milestone> mocks = new ArrayList<Milestone>(count);
		for (int x = 0; x < count; ++x) {
			Milestone mock = populate(new Milestone(), entityManager, parentProd);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static Milestone populate(Milestone milestone, EntityManager entityManager, Product parentProd) {
		int index = created.incrementAndGet();

		milestone.setValue("Milestone" + index);
		milestone.setSortkey((short) 0);
		milestone.setProduct(parentProd);

		return milestone;
	}
}
