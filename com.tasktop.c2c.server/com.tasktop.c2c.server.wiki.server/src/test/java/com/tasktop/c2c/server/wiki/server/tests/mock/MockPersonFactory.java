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
package com.tasktop.c2c.server.wiki.server.tests.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.wiki.server.domain.Person;


public class MockPersonFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static Person create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Person> create(EntityManager entityManager, int count) {
		List<Person> mocks = new ArrayList<Person>(count);
		for (int x = 0; x < count; ++x) {
			Person mock = populate(new Person(), entityManager);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static Person populate(Person person, EntityManager entityManager) {
		int index = created.incrementAndGet();

		person.setIdentity("person" + index + "@example.com");
		person.setName("First" + index + " Last" + index);

		return person;
	}
}
