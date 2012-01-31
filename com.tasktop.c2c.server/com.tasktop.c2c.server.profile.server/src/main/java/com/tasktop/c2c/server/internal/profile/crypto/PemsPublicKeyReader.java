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
package com.tasktop.c2c.server.internal.profile.crypto;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.jclouds.crypto.Pems;

import com.google.common.io.ByteStreams;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

public class PemsPublicKeyReader implements PublicKeyReader {

	@Override
	public SshPublicKey readPublicKey(String keySpec) {
		try {
			KeySpec publicKeySpec = Pems.publicKeySpec(ByteStreams.newInputStreamSupplier(keySpec.getBytes("utf-8")));
			if (publicKeySpec instanceof RSAPublicKeySpec) {
				RSAPublicKeySpec rsaPublicKeySpec = (RSAPublicKeySpec) publicKeySpec;
				PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
				SshPublicKey key = new SshPublicKey();
				key.setAlgorithm(publicKey.getAlgorithm());
				key.setKeyData(publicKey.getEncoded());
				return key;
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			// ignore
		} catch (RuntimeException e) {
			// ignore
		} catch (InvalidKeySpecException e) {
			// ignore
		} catch (NoSuchAlgorithmException e) {
			// ignore
		}
		return null;
	}
}
