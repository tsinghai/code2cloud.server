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
package com.tasktop.c2c.server.common.service.domain;

import java.io.Serializable;


/**
 * Contains information about the sort of a query.
 * 
 */
public class SortInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Order {
		ASCENDING, DESCENDING
	};

	private String sortField;
	private Order sortOrder;

	public SortInfo() {
	}

	public SortInfo(String sortField) {
		this(sortField, Order.ASCENDING);
	}

	public SortInfo(String sortField, Order sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public Order getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Order sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sortField == null) ? 0 : sortField.hashCode());
		result = prime * result + ((sortOrder == null) ? 0 : sortOrder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SortInfo other = (SortInfo) obj;
		if (sortField == null) {
			if (other.sortField != null)
				return false;
		} else if (!sortField.equals(other.sortField))
			return false;
		if (sortOrder != other.sortOrder)
			return false;
		return true;
	}

	@Override
	public String toString() {
		ToStringCreator result = new ToStringCreator(this);
		result.append("sortField", sortField);
		result.append("sortOrder", sortOrder);
		return result.toString();
	}

}
