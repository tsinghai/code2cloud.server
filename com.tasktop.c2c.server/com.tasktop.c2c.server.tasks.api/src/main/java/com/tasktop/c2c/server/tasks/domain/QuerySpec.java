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
package com.tasktop.c2c.server.tasks.domain;

import java.io.Serializable;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.ToStringCreator;


@SuppressWarnings("serial")
public class QuerySpec implements Serializable {
	private Region region;
	private SortInfo sortInfo;
	private Boolean thin;

	public QuerySpec() {
		// nothing
	}

	public QuerySpec(Region region, SortInfo sortInfo, boolean thin) {
		this.region = region;
		this.sortInfo = sortInfo;
		this.thin = thin;
	}

	public QuerySpec(Region region, SortInfo sortInfo) {
		this(region, sortInfo, true);
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public SortInfo getSortInfo() {
		return sortInfo;
	}

	public void setSortInfo(SortInfo sortInfo) {
		this.sortInfo = sortInfo;
	}

	public Boolean getThin() {
		return thin;
	}

	public void setThin(Boolean thin) {
		this.thin = thin;
	}

	@Override
	public String toString() {
		ToStringCreator result = new ToStringCreator(this);
		result.append("region", region);
		result.append("sortInfo", sortInfo);
		result.append("thin", thin);
		return result.toString();
	}
}
