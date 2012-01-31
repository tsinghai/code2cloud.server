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
package com.tasktop.c2c.server.common.service.identity;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class Gravatar {
	public static String computeHash(String emailAdress) {
		// algorithm based on Mylyn Identity framework
		String gravatarHash = null;
		if (emailAdress != null) {
			try {
				byte[] emailBytes = emailAdress.trim().toLowerCase().getBytes("CP1252");
				byte[] digest = MessageDigest.getInstance("MD5").digest(emailBytes);
				String hash = new BigInteger(1, digest).toString(16);
				while (hash.length() < 32) {
					hash = '0' + hash;
				}
				gravatarHash = hash;
			} catch (UnsupportedEncodingException e) {
				// ignore
			} catch (NoSuchAlgorithmException e) {
				// ignore
			}
		}
		return gravatarHash;
	}
}
