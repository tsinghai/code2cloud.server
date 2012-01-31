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

import com.tasktop.c2c.server.internal.tasks.domain.Profile;

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

		String username = "test" + index;
		profile.setLoginName(username);
		profile.setRealname("Test User " + index);
		profile.setCryptpassword("123456");
		profile.setDisabledtext("");
		profile.setDisableMail(false);

		return profile;
	}

}
