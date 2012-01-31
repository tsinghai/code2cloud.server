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

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

import com.tasktop.c2c.server.internal.profile.crypto.OpenSSHPublicKeyReader;
import com.tasktop.c2c.server.internal.profile.crypto.Rfc4253Reader;
import com.tasktop.c2c.server.internal.profile.crypto.Rfc4253Writer;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

public class OpenSSHPublicKeyReaderTest {

	@Test
	public void testSimple() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String key = "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAvcVV9k7OMpIhg3+dqv93wUNgMwdfK3FEltUXxiRo7ziGJiHwGHXeGFDaoie2Gw6vbpJy5Fo/x+Iw9oVdAFtdQDWjNaOIiBaG3OUsjzHXv0UWWb+LqmtzAmXc3zLhCjCYDB3WWle2FS2ulg7O0cl2l2uvx6b4SHOD6x05TTQnGRxS3s6Bx9uy6IkCcMW1PZ+ddTjIZ7VuXra9bkqyJynNWBWh5m8kNYtN9N58b2zJ7t81TA63vVIsZoX699TJ17ADMtPAuoodsOJEubE9YrAy07DwqvM/bK02TMz0wttkfDk82TDyS2us7EzJsIwN2UfGnGpW6lPpbyWsDtGjKcYm7Q== David Green@DAVIDGREENT410S\n";
		validateKeyOk(key, 2048);
	}

	@Test
	public void testTask_2983() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String key = "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAIEAxyu7zpQJQhXAcXqeWjU1Fmssw2+4qjfAWyKkqzET5UZez3DLfH+X/cm6NbEk85t/CTntWuLRPHYHuyhiGu9aBfKgr0Uo1aiTP7p76KWVcWVWdM0qMaeMdCqTwPFq7pWI5CpfZzODbbjdyJ/LwV4+UNONvr3ppzAE0g9fXtbaEwk= gowlin@pascal\n";
		validateKeyOk(key, 1024);
	}

	private void validateKeyOk(String key, int expectedKeySize) throws NoSuchAlgorithmException,
			InvalidKeySpecException {
		SshPublicKey publicKey = new OpenSSHPublicKeyReader().readPublicKey(key);
		assertNotNull(publicKey);
		assertEquals("RSA", publicKey.getAlgorithm());
		PublicKey pk = publicKey.getPublicKey();
		assertNotNull(pk);
		assertEquals("RSA", pk.getAlgorithm());
		assertTrue(pk instanceof RSAPublicKey);

		RSAPublicKey rsapk = (RSAPublicKey) pk;
		assertEquals(expectedKeySize, rsapk.getModulus().bitLength());

		String originalText = new OpenSSHPublicKeyReader().computeEncodedKeyText(publicKey);
		System.out.println(originalText);
		assertTrue(key.startsWith(originalText));
	}

	@Test
	public void testUByte() {
		for (int i = 0; i <= 255; i++) {
			byte b = Rfc4253Writer.toSignedByte(i);
			int back = Rfc4253Reader.toUnsignedByte(b);
			assertEquals(i, back);
		}
	}

	@Test
	public void testWriterAndReader() throws IOException {

		for (int i = 0; i < 1000; i += 31) {
			BigInteger original = new BigInteger(i + "");
			Rfc4253Writer writer = new Rfc4253Writer();
			writer.writeMpint(original);
			Rfc4253Reader reader = new Rfc4253Reader(writer.getBytes(), 0);
			BigInteger back = reader.readMpint();
			assertEquals(original, back);
		}

	}

}
