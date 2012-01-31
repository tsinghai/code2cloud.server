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
package com.tasktop.c2c.server.profile.tests.domain.mock;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.profile.domain.internal.Project;


public class MockProjectFactory {

	private static int created = 0;

	public static Project create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Project> create(EntityManager entityManager, int count) {
		List<Project> mocks = new ArrayList<Project>(count);
		for (int x = 0; x < count; ++x) {
			Project mock = populate(new Project());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Project populate(Project mock) {
		int index = ++created;
		mock.setName("project" + index);
		mock.computeIdentifier();
		mock.setDescription("A description about project " + index);
		mock.setPublic(Boolean.FALSE);
		return mock;
	}

}
