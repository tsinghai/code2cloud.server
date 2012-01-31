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
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

/**
 * an implementation of <a href="http://www.ietf.org/rfc/rfc4253.txt">RFC 4253</a>
 * 
 * @author David Green
 * @see http://www.ietf.org/rfc/rfc4253.txt
 */
public class OpenSSHPublicKeyReader implements PublicKeyReader {

	private static final byte[] FORMAT = new byte[] { 's', 's', 'h', '-', 'r', 's', 'a' };

	public SshPublicKey readPublicKey(String keySpec) {
		keySpec = keySpec.trim();
		String[] parts = keySpec.split(" ");
		if (parts.length >= 2) {
			String algorithm = parts[0];
			String base64Data = parts[1];
			if (algorithm.equals("ssh-rsa")) {
				SshPublicKey sshPublicKey = new SshPublicKey();
				sshPublicKey.setAlgorithm("RSA");
				byte[] decodedData = Base64.decodeBase64(StringUtils.getBytesUtf8(base64Data));

				Rfc4253Reader reader = new Rfc4253Reader(decodedData, 0);

				try {
					byte[] format = reader.readBytes();
					byte[] exponent = reader.readBytes();
					byte[] modulus = reader.readBytes();

					if (Arrays.equals(FORMAT, format)) {
						BigInteger exp = new BigInteger(exponent);
						BigInteger mod = new BigInteger(modulus);
						RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(mod, exp);
						try {
							PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
							sshPublicKey.setKeyData(publicKey.getEncoded());
							return sshPublicKey;
						} catch (InvalidKeySpecException t) {
							getLogger().warn("Invalid key spec: " + t.getMessage(), t);
						} catch (NoSuchAlgorithmException t) {
							getLogger().warn("Invalid algorithm: " + t.getMessage(), t);
						}
					}

				} catch (IOException e) {
					// ignore
				}
			}
		}
		return null;
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(OpenSSHPublicKeyReader.class);
	}

	public String computeEncodedKeyText(SshPublicKey key) {
		try {
			BigInteger exp = ((RSAPublicKey) key.getPublicKey()).getPublicExponent();
			BigInteger mod = ((RSAPublicKey) key.getPublicKey()).getModulus();
			Rfc4253Writer writer = new Rfc4253Writer();
			writer.writeBytes(FORMAT);
			writer.writeMpint(exp);
			writer.writeMpint(mod);
			return "ssh-rsa " + StringUtils.newStringUtf8(Base64.encodeBase64(writer.getBytes()));
		} catch (Exception e) {
			return null;
		}
	}
}
