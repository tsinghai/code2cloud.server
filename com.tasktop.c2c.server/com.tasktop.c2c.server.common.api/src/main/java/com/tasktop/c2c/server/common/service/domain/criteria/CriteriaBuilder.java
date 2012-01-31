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

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;

/** Utility class to construct criteria objects. */
public class CriteriaBuilder {

	public Criteria result = null;

	public CriteriaBuilder() {

	}

	public Criteria toCriteria() {
		return result;
	}

	public CriteriaBuilder column(String columnName, Operator op,
			Object columnValue) {
		if (result != null) {
			throw new IllegalStateException(
					"Already have a column criteria, perhaps you want to AND or OR with it?");
		}
		result = new ColumnCriteria(columnName, op, columnValue);
		return this;
	}

	public CriteriaBuilder column(String columnName, Object columnValue) {
		return column(columnName, Operator.EQUALS, columnValue);
	}

	public CriteriaBuilder and(Criteria crit) {
		if (result == null) {
			throw new IllegalStateException(
					"Need a column crit first before it can be and-ed, (call column first)?");
		}
		if (result instanceof NaryCriteria
				&& result.getOperator().equals(Operator.AND)) {
			((NaryCriteria) result).getSubCriteria().add(crit);
		} else {
			result = new NaryCriteria(Operator.AND, result, crit);
		}
		return this;
	}

	public CriteriaBuilder and(String columnName, Object columnValue) {
		return and(columnName, Operator.EQUALS, columnValue);
	}

	public CriteriaBuilder and(String columnName, Operator op,
			Object columnValue) {
		return and(new ColumnCriteria(columnName, op, columnValue));
	}

	public CriteriaBuilder or(String columnName, Operator op, Object columnValue) {
		if (result == null) {
			throw new IllegalStateException(
					"Need a column crit first before it can be or-ed, (call column first)?");
		}
		if (op.isUnary()) {
			throw new IllegalStateException();
		}
		if (result instanceof NaryCriteria
				&& result.getOperator().equals(Operator.OR)) {
			((NaryCriteria) result).getSubCriteria().add(
					new ColumnCriteria(columnName, op, columnValue));
		} else {
			result = new NaryCriteria(Operator.OR, result, new ColumnCriteria(
					columnName, op, columnValue));
		}
		return this;
	}

	public CriteriaBuilder or(String columnName, Object columnValue) {
		return or(columnName, Operator.EQUALS, columnValue);
	}

}
