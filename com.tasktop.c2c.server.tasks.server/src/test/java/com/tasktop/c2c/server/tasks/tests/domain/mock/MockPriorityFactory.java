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

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.tasks.domain.Classification;
import com.tasktop.c2c.server.internal.tasks.domain.Priority;


public class MockPriorityFactory {

	private static int created = 0;

	public static Priority create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Priority> create(EntityManager entityManager, int count) {
		List<Priority> mocks = new ArrayList<Priority>(count);
		for (int x = 0; x < count; ++x) {
			Priority mock = populate(new Priority());
			if (entityManager != null) {
				entityManager.persist(mock);
			}

			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Priority populate(Priority priority) {
		int index = ++created;

		priority.setValue("Priority" + index);
		priority.setSortkey((short) index);
		priority.setIsactive(true);

		return priority;
	}

	private static Classification createClassification() {
		Classification classifications = new Classification();
		classifications.setName("None");
		return classifications;
	}

}
