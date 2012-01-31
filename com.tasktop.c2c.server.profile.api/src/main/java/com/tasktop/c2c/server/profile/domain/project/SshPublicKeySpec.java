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

import java.io.Serializable;

/**
 * A key specification
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
@SuppressWarnings("serial")
public class SshPublicKeySpec implements Serializable {

	private String name;
	private String keyData;

	public SshPublicKeySpec() {
	}

	public SshPublicKeySpec(String name, String keyData) {
		this.name = name;
		this.keyData = keyData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeyData() {
		return keyData;
	}

	public void setKeyData(String keyData) {
		this.keyData = keyData;
	}

}
