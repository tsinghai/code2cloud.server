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
package com.tasktop.c2c.server.profile.tests.crypto;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

import com.tasktop.c2c.server.internal.profile.crypto.Rfc4716PublicKeyReader;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;


public class Rfc4716PublicKeyReaderTest {

	@Test
	public void testSimple() throws NoSuchAlgorithmException, InvalidKeySpecException {
		// example taken from http://www.ietf.org/rfc/rfc4716.txt
		String key = "---- BEGIN SSH2 PUBLIC KEY ----\n" + //
				"Comment: \"key created for unit test\"\n" //
				+ "x-command: /foo/bar/b.sh\n" //
				+ "AAAAB3NzaC1yc2EAAAABIwAAAQEAvcVV9k7OMpIhg3+dqv93wUNgMwdfK3FEltUXxiRo7ziG\n" //
				+ "JiHwGHXeGFDaoie2Gw6vbpJy5Fo/x+Iw9oVdAFtdQDWjNaOIiBaG3OUsjzHXv0UWWb+LqmtzAmXc3zLh\n" //
				+ "CjCYDB3WWle2FS2ulg7O0cl2l2uvx6b4SHOD6x05TTQnGRxS3s6Bx9uy6IkCcMW1PZ+ddTjIZ7VuXra9\n" //
				+ "bkqyJynNWBWh5m8kNYtN9N58b2zJ7t81TA63vVIsZoX699TJ17ADMtPAuoodsOJEubE9YrAy07DwqvM/\n" //
				+ "bK02TMz0wttkfDk82TDyS2us7EzJsIwN2UfGnGpW6lPpbyWsDtGjKcYm7Q==\n" //
				+ "---- END SSH2 PUBLIC KEY ----";
		validateKeyOk(key, 2048);
	}

	private void validateKeyOk(String key, int expectedKeySize) throws NoSuchAlgorithmException,
			InvalidKeySpecException {
		SshPublicKey publicKey = new Rfc4716PublicKeyReader().readPublicKey(key);
		assertNotNull(publicKey);
		assertEquals("RSA", publicKey.getAlgorithm());
		PublicKey pk = publicKey.getPublicKey();
		assertNotNull(pk);
		assertEquals("RSA", pk.getAlgorithm());
		assertTrue(pk instanceof RSAPublicKey);

		RSAPublicKey rsapk = (RSAPublicKey) pk;
		assertEquals(expectedKeySize, rsapk.getModulus().bitLength());
	}
}
