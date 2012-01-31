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

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class TrimmingHexEncodingTextEncryptor implements TextEncryptor {

	private final TextEncryptor internalEncryptor;

	public TrimmingHexEncodingTextEncryptor(String password, String salt) {
		internalEncryptor = Encryptors.text(password, salt);
	}

	@Override
	public String encrypt(String text) {
		// Pass straight through to our encryptor.
		return internalEncryptor.encrypt(text);
	}

	@Override
	public String decrypt(String encryptedText) {
		// Trim the result which comes back from this decryptor. An issue was seen which caused Spring Social's
		// connection repository to fail due to trailing whitespace on a string - this is a fix for that issue.
		return internalEncryptor.decrypt(encryptedText).trim();
	}
}
