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

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Criteria extends Serializable {
	static final long serialVersionUID = 1L;

	public enum Operator {
		AND("AND"), OR("OR"), EQUALS("="), NOT_EQUALS("!="), STRING_CONTAINS("CONTAINS"), NOT("NOT"), LESS_THAN("<"), GREATER_THAN(
				">");

		private String queryString;

		private Operator(String queryString) {
			this.queryString = queryString;
		}

		public boolean isUnary() {
			return this == NOT;
		}

		public String toQueryString() {
			return queryString;
		}

		public static Operator fromQueryString(String word) {
			for (Operator op : Operator.values()) {
				if (op.toQueryString().equals(word)) {
					return op;
				}
			}
			return null;
		}
	}

	Operator getOperator();

	void setOperator(Operator operator);

	void validate();

	String toQueryString();

}
