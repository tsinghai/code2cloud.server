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
package com.tasktop.c2c.server.profile.domain.validation;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

public class SshPublicKeyValidator implements Validator {

	private static final int MAX_RSA_KEY_SIZE = 8096;
	private static final int MIN_RSA_KEY_SIZE = 2048;

	@Override
	public boolean supports(Class<?> clazz) {
		return SshPublicKey.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SshPublicKey sshPublicKey = (SshPublicKey) target;
		ValidationUtils.rejectIfEmpty(errors, "algorithm", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "name", "field.required");
		ValidationUtils.rejectIfEmpty(errors, "keyData", "field.required");

		if (sshPublicKey.getKeyData() != null && sshPublicKey.getAlgorithm() != null) {
			try {
				PublicKey publicKey = sshPublicKey.getPublicKey();
				if (publicKey instanceof RSAPublicKey) {
					RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
					int keySize = rsaPublicKey.getModulus().bitLength();
					if (keySize < MIN_RSA_KEY_SIZE || keySize > MAX_RSA_KEY_SIZE) {
						errors.rejectValue("keyData", "invalidRSAKeySize", new Object[] { MIN_RSA_KEY_SIZE,
								MAX_RSA_KEY_SIZE }, null);
					}
				} else {
					// not supported
					errors.rejectValue("algorithm", "invalidPublicKeyAlgorithm");
				}
			} catch (NoSuchAlgorithmException e) {
				errors.rejectValue("algorithm", "invalidPublicKeyAlgorithm");
			} catch (InvalidKeySpecException e) {
				errors.rejectValue("keyData", "invalidPublicKeyData");
			}
		}
	}
}
