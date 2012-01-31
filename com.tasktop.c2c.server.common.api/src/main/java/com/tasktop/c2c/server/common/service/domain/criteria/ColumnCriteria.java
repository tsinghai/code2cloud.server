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
package com.tasktop.c2c.server.common.service.domain.criteria;

import java.util.Date;

@SuppressWarnings("serial")
public class ColumnCriteria extends AbstractCriteria {
	private String columnName;
	private Object columnValue;

	public ColumnCriteria() {
		// Nothing
	}

	/**
	 * Create a {@link ColumnCriteria} using an {@link Operator} or your choice.
	 * 
	 * @param columnName
	 * @param op
	 * @param columValue
	 */
	public ColumnCriteria(String columnName, Operator op, Object columValue) {
		this.columnName = columnName;
		this.columnValue = columValue;
		setOperator(op);
	}

	/**
	 * Create a {@link ColumnCriteria} using an {@link Operator.EQUALS} operator.
	 * 
	 * @param columnName
	 * @param columValue
	 */
	public ColumnCriteria(String columnName, Object columValue) {
		this(columnName, Operator.EQUALS, columValue);
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Object getColumnValue() {
		return columnValue;
	}

	public void setColumnValue(Object columnValue) {
		this.columnValue = columnValue;
	}

	public void validate() {
		if (getOperator() == null) {
			throw new IllegalStateException();
		}
		if (getOperator().isUnary()) {
			throw new IllegalStateException();
		}
		if (columnName == null) {
			throw new IllegalStateException();
		}
		if (columnValue == null) {
			throw new IllegalStateException();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((columnValue == null) ? 0 : columnValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnCriteria other = (ColumnCriteria) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (columnValue == null) {
			if (other.columnValue != null)
				return false;
		} else if (!columnValue.equals(other.columnValue))
			return false;
		return true;
	}

	public String toQueryString() {
		String valuePart;
		if (columnValue == null) {
			valuePart = "NULL";
		} else if (columnValue instanceof String) {
			valuePart = "'" + columnValue + "'";
		} else if (columnValue instanceof Date) {
			valuePart = "date:" + ((Date) columnValue).getTime();
		} else {
			valuePart = columnValue.toString();
		}
		return columnName + " " + getOperator().toQueryString() + " " + valuePart;
	}

}
