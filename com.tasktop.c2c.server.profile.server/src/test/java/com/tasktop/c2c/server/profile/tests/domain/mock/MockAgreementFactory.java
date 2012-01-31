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
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.profile.domain.internal.Agreement;


public class MockAgreementFactory {

	private static int created = 0;

	public static Agreement create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Agreement> create(EntityManager entityManager, int count) {
		List<Agreement> mocks = new ArrayList<Agreement>(count);
		for (int x = 0; x < count; ++x) {
			Agreement mock = populate(new Agreement());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Agreement populate(Agreement mock) {
		int index = ++created;
		mock.setTitle("Agreement " + index);
		mock.setRank(index);
		mock.setText("The text for Agreement " + index);
		mock.setDateCreated(new Date());
		mock.setActive(true);
		return mock;
	}

}
