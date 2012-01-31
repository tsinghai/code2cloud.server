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
package com.tasktop.c2c.server.cloud.domain;

import java.util.Set;

import com.tasktop.c2c.server.common.service.domain.ToStringCreator;

public class ServiceHost {

	private Long id;
	private String internalNetworkAddress;
	private boolean available;
	private Set<ServiceType> supportedServices;

	/**
	 * the cloud-private network address, used for accessing this host within the cloud.
	 */
	public String getInternalNetworkAddress() {
		return internalNetworkAddress;
	}

	public void setInternalNetworkAddress(String internalNetworkAddress) {
		this.internalNetworkAddress = internalNetworkAddress;
	}

	/**
	 * Indicate if the host is available for servicing requests. When true, the host is expected to be available. When
	 * false the host is known to be unavailable, usually because it has not been provisioned or has been turned off for
	 * maintenance, etc.
	 */
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.id).append("addr", this.internalNetworkAddress).toString();
	}

	/**
	 * @return the supportedServices
	 */
	public Set<ServiceType> getSupportedServices() {
		return supportedServices;
	}

	/**
	 * @param supportedServices
	 *            the supportedServices to set
	 */
	public void setSupportedServices(Set<ServiceType> supportedServices) {
		this.supportedServices = supportedServices;
	}

}
