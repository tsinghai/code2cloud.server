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

import java.util.Date;

/**
 * A record of a user's agreement to a legal agreement.
 * 
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class AgreementProfile extends AbstractEntity {

	private Date agreementDate;
	private Agreement agreement;

	public void setAgreementDate(Date agreementDate) {
		this.agreementDate = agreementDate;
	}

	public Date getAgreementDate() {
		return agreementDate;
	}

	public void setAgreement(Agreement agreement) {
		this.agreement = agreement;
	}

	public Agreement getAgreement() {
		return agreement;
	}
}
