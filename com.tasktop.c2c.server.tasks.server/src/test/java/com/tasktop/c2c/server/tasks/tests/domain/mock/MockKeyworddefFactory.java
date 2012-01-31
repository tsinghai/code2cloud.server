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

import com.tasktop.c2c.server.internal.tasks.domain.Keyworddef;


public class MockKeyworddefFactory {

	private static int created = 0;

	public static Keyworddef create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Keyworddef> create(EntityManager entityManager, int count) {
		List<Keyworddef> mocks = new ArrayList<Keyworddef>(count);
		for (int x = 0; x < count; ++x) {
			Keyworddef mock = populate(new Keyworddef());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Keyworddef populate(Keyworddef keyworddef) {
		int index = ++created;

		keyworddef.setName("keyword" + index);
		keyworddef.setDescription(keyworddef.getName().toUpperCase().substring(0, 1)
				+ keyworddef.getName().substring(1));

		return keyworddef;
	}

}
