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
package com.tasktop.c2c.server.profile.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {

	private Credentials credentials;
	private Boolean hasPendingAgreements;

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setHasPendingAgreements(Boolean hasPendingAgreements) {
		this.hasPendingAgreements = hasPendingAgreements;
	}

	public Boolean getHasPendingAgreements() {
		return hasPendingAgreements;
	}

}
