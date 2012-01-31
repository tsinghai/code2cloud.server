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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;


public class MockSshPublicKeyFactory {

	private static AtomicInteger seed = new AtomicInteger(0);

	public static SshPublicKey create(EntityManager entityManager, Profile profile) {
		return create(entityManager, profile, 1).get(0);
	}

	public static List<SshPublicKey> create(EntityManager entityManager, Profile profile, int count) {
		List<SshPublicKey> mocks = new ArrayList<SshPublicKey>(count);
		for (int x = 0; x < count; ++x) {
			SshPublicKey mock = populate(profile, new SshPublicKey());
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static SshPublicKey populate(Profile profile, SshPublicKey mock) {

		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
		PublicKey publicKey = keyPair.getPublic();

		mock.setName("test " + seed.getAndIncrement());
		mock.setAlgorithm(publicKey.getAlgorithm());
		mock.setKeyData(publicKey.getEncoded());
		mock.computeFingerprint();

		if (profile != null) {
			profile.getSshPublicKeys().add(mock);
			mock.setProfile(profile);
		}

		return mock;
	}

}
