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

import com.tasktop.c2c.server.profile.domain.internal.Profile;

public class MockProfileFactory {

	private static int created = 0;

	public static Profile create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Profile> create(EntityManager entityManager, int count) {
		List<Profile> mocks = new ArrayList<Profile>(count);
		for (int x = 0; x < count; ++x) {
			Profile mock = populate(new Profile());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Profile populate(Profile profile) {
		int index = ++created;
		profile.setEmail("foo" + index + "@example.com");
		profile.setFirstName("Joe" + index);
		profile.setLastName("Bloe" + index);
		profile.setPassword("123456aA@" + index);
		profile.setUsername(profile.getEmail());
		profile.setEmailVerified(false);
		return profile;
	}

}
