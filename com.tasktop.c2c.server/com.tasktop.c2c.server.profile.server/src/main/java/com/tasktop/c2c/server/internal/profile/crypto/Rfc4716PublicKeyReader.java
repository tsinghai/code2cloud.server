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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

/**
 * an implementation of <a href="http://www.ietf.org/rfc/rfc4716.txt">RFC 4716</a>
 * 
 * @author David Green
 * @see http://www.ietf.org/rfc/rfc4716.txt
 */
public class Rfc4716PublicKeyReader implements PublicKeyReader {

	private static final String START_MARKER = "---- BEGIN SSH2 PUBLIC KEY ----";
	private static final String END_MARKER = "---- END SSH2 PUBLIC KEY ----";
	private static final Pattern HEADER_PATTERN = Pattern.compile("(\\S{1,64}): .*");

	/**
	 * read a public key from the given text
	 * 
	 * @param keySpec
	 *            the key specification.
	 * @return the key or null if no key was found
	 */
	public SshPublicKey readPublicKey(String keySpec) {
		BufferedReader reader = new BufferedReader(new StringReader(keySpec));
		String line;
		SshPublicKey key = null;
		boolean endFound = false;
		boolean dataStarted = false;
		boolean continueHeader = false;
		String base64Data = "";
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				// skip blank lines. They shouldn't really be in there, but if they are we can ignore them.
				if (line.length() != 0) {
					if (key == null) {
						if (line.equals(START_MARKER)) {
							key = new SshPublicKey();
						}
					} else {
						if (line.equals(END_MARKER)) {
							endFound = true;
							break;
						} else if (!dataStarted && (continueHeader || HEADER_PATTERN.matcher(line).matches())) {
							// skip headers
							continueHeader = line.endsWith("\"");
						} else {
							dataStarted = true;

							base64Data += line;
						}
					}
				}
			}
			if (!endFound) {
				key = null;
			} else {
				if (base64Data.length() > 0) {
					byte[] keyData = Base64.decodeBase64(StringUtils.getBytesUtf8(base64Data));

					Rfc4253Reader keyReader = new Rfc4253Reader(keyData, 0);
					String algorithm = keyReader.readString();
					if ("ssh-rsa".equals(algorithm)) {
						key.setAlgorithm("RSA");

						BigInteger exponent = keyReader.readMpint();
						BigInteger modulus = keyReader.readMpint();

						try {
							KeyFactory keyFactory = KeyFactory.getInstance(key.getAlgorithm());
							RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
							PublicKey publicKey = keyFactory.generatePublic(rsaPublicKeySpec);

							byte[] encoded = publicKey.getEncoded();

							key.setKeyData(encoded);

						} catch (InvalidKeySpecException t) {
							getLogger().warn("Invalid key spec: " + t.getMessage(), t);
						} catch (NoSuchAlgorithmException t) {
							getLogger().warn("Invalid algorithm: " + t.getMessage(), t);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		if (key == null || key.getAlgorithm() == null || key.getKeyData() == null) {
			key = null;
		}
		return key;
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(Rfc4716PublicKeyReader.class);
	}
}
