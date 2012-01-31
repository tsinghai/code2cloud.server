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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class ServiceHost extends BaseEntity {

	private List<ProjectService> projectServices = new ArrayList<ProjectService>();

	private ServiceHostConfiguration serviceHostConfiguration;
	private String internalNetworkAddress;
	private boolean available;

	/**
	 * the services run by this host
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH }, mappedBy = "serviceHost")
	public List<ProjectService> getProjectServices() {
		return projectServices;
	}

	public void setProjectServices(List<ProjectService> projectServices) {
		this.projectServices = projectServices;
	}

	public void add(ProjectService service) {
		service.setServiceHost(this);
		getProjectServices().add(service);
	}

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

	@ManyToOne
	public ServiceHostConfiguration getServiceHostConfiguration() {
		return serviceHostConfiguration;
	}

	/**
	 * @param serviceHostConfiguration
	 *            the serviceHostConfiguration to set
	 */
	public void setServiceHostConfiguration(ServiceHostConfiguration serviceHostConfiguration) {
		this.serviceHostConfiguration = serviceHostConfiguration;
	}
}
