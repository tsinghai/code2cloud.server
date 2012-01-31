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
package com.tasktop.c2c.server.deployment.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author terry.denney (Tasktop Technologies Inc.)
 * 
 */
public class DeploymentServiceConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String description;
	private String type;
	private String vendor;
	private String version;
	private List<ServiceTier> tiers;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<ServiceTier> getTiers() {
		return tiers;
	}

	public void setTiers(List<ServiceTier> tiers) {
		this.tiers = tiers;
	}

	public static class ServiceTier implements Serializable {

		private static final long serialVersionUID = 1L;

		private String description;
		private String pricingPeriod;
		private String pricingType;
		private String type;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getPricingPeriod() {
			return pricingPeriod;
		}

		public void setPricingPeriod(String pricingPeriod) {
			this.pricingPeriod = pricingPeriod;
		}

		public String getPricingType() {
			return pricingType;
		}

		public void setPricingType(String pricingType) {
			this.pricingType = pricingType;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

}
