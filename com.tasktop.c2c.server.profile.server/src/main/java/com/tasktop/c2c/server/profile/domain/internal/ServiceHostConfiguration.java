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

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.tasktop.c2c.server.cloud.domain.ServiceType;

@Entity
public class ServiceHostConfiguration extends BaseEntity {

	private int pendingAllocations;
	private int pendingDeletions;
	private Set<ServiceType> supportedServices;

	@ElementCollection
	@CollectionTable(name = "SERVICEHOSTCONFIGURATIONSERVICES")
	@Column
	@Enumerated(EnumType.STRING)
	public Set<ServiceType> getSupportedServices() {
		return supportedServices;
	}

	public void setSupportedServices(Set<ServiceType> supportedServices) {
		this.supportedServices = supportedServices;
	}

	@Column
	public int getPendingAllocations() {
		return pendingAllocations;
	}

	public void setPendingAllocations(int pendingAllocations) {
		this.pendingAllocations = pendingAllocations;
	}

	@Column
	public int getPendingDeletions() {
		return pendingDeletions;
	}

	public void setPendingDeletions(int pendingDeletions) {
		this.pendingDeletions = pendingDeletions;
	}

}
