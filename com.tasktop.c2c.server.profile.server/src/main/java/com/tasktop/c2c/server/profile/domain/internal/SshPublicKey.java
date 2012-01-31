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
package com.tasktop.c2c.server.profile.domain.internal;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Storage for SSH public keys, based on RFC 4716.
 * 
 * @see http://www.ietf.org/rfc/rfc4716.txt
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
@Entity
public class SshPublicKey extends BaseEntity {

	private Profile profile;
	private String name;
	private String algorithm;
	private String fingerprint;
	private byte[] keyData;
	@Transient
	private PublicKey publicKey;

	/**
	 * see RFC 4716 section 4, based on RFC 1321 and RFC 4253
	 */
	@Basic(optional = false)
	@Column(nullable = false, updatable = false)
	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	/**
	 * the name that helps the user to identify their key
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Basic(optional = false)
	@Column(nullable = false, updatable = false)
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	@Basic(optional = false)
	@Column(nullable = false, updatable = false)
	public byte[] getKeyData() {
		return keyData;
	}

	public void setKeyData(byte[] keyData) {
		this.keyData = keyData;
	}

	public boolean isSameAs(PublicKey publicKey) {
		if (algorithm == null && !algorithm.equals(publicKey.getAlgorithm())) {
			return false;
		}
		byte[] publicKeyEncoded = publicKey.getEncoded();
		return Arrays.equals(publicKeyEncoded, keyData);
	}

	@Transient
	public PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (publicKey == null && getAlgorithm() != null) {
			KeyFactory keyFactory = KeyFactory.getInstance(getAlgorithm());
			if (getAlgorithm().equals("RSA")) {
				X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(getKeyData());
				publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
			}
		}
		return publicKey;
	}

	public void computeFingerprint() {
		MessageDigest messageDigest;
		PublicKey publicKey;
		try {
			publicKey = getPublicKey();
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
		byte[] digest = messageDigest.digest(publicKey.getEncoded());
		String result = "";
		for (byte b : digest) {
			if (result.length() != 0) {
				result += ":";
			}
			int i = b & 0xff;
			String hex = Integer.toHexString(i);
			if (hex.length() == 1) {
				result += '0';
			}
			result += hex;
		}
		setFingerprint(result);
	}

	/**
	 * the profile of the user that owns this public key
	 */
	@ManyToOne
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "SshPublicKey [name=" + name + ", algorithm=" + algorithm + ", fingerprint=" + fingerprint
				+ ", publicKey=" + publicKey + ", id=" + getId() + ", version=" + getVersion() + "]";
	}

}
