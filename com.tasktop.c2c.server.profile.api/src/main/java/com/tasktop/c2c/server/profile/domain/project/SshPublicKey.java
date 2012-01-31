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
package com.tasktop.c2c.server.profile.domain.project;

/**
 * An SSH public key
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
@SuppressWarnings("serial")
public class SshPublicKey extends AbstractEntity {

	private String algorithm;
	private String name;
	private String fingerprint;
	private String keyText;

	/**
	 * the name that helps a user to identify their key
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	/**
	 * @return the keyText
	 */
	public String getKeyText() {
		return keyText;
	}

	/**
	 * @param keyText
	 *            the keyText to set
	 */
	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

}
