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
import java.util.UUID;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;


public class MockProjectInvitationTokenFactory {

	private static int created = 0;

	public static InvitationToken create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<InvitationToken> create(EntityManager entityManager, int count) {
		List<InvitationToken> mocks = new ArrayList<InvitationToken>(count);
		for (int x = 0; x < count; ++x) {
			InvitationToken mock = populate(new InvitationToken());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static InvitationToken populate(InvitationToken mock) {
		int index = ++created;
		mock.setProject(null);
		mock.setDateCreated(new Date());
		mock.setEmail("email+" + index + "@example.com");
		mock.setIssuingProfile(null);
		mock.setToken(UUID.randomUUID().toString());
		return mock;
	}
}
