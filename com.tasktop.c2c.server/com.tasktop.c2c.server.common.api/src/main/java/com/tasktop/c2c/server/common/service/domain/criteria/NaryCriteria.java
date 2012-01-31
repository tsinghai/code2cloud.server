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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class NaryCriteria extends AbstractCriteria {
	private List<Criteria> subCriteria;

	public NaryCriteria() {
		// Nothing
	}

	/**
	 * Create an n-ary criteria clause with the specified {@link Operator} with n sub-criteria.
	 * 
	 * @param op
	 * @param subCritieria
	 */
	public NaryCriteria(Operator op, Criteria... subCritieria) {
		setOperator(op);
		setSubCriteria(new ArrayList<Criteria>(Arrays.asList(subCritieria)));
	}

	public void validate() {
		if (getOperator() == null) {
			throw new IllegalStateException();
		}
		if (!hasSubCriteria()) {
			throw new IllegalStateException();
		}
		if (getOperator().isUnary() && getSubCriteria().size() > 1) {
			throw new IllegalStateException();
		}
		for (Criteria criteria : getSubCriteria()) {
			criteria.validate();
		}
	}

	public List<Criteria> getSubCriteria() {
		return subCriteria;
	}

	public void setSubCriteria(List<Criteria> subCriteria) {
		this.subCriteria = subCriteria;
	}

	public void addSubCriteria(Criteria crit) {
		if (this.subCriteria == null) {
			this.subCriteria = new ArrayList<Criteria>();
		}
		this.subCriteria.add(crit);
	}

	public boolean hasSubCriteria() {
		return subCriteria != null && !subCriteria.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subCriteria == null) ? 0 : subCriteria.hashCode());
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
		NaryCriteria other = (NaryCriteria) obj;
		if (subCriteria == null) {
			if (other.subCriteria != null)
				return false;
		} else if (!subCriteria.equals(other.subCriteria))
			return false;
		return true;
	}

	public String toQueryString() {
		String s = "";
		if (subCriteria != null) {
			for (Criteria c : subCriteria) {
				if (s.length() > 0 || getOperator().isUnary()) {
					if (s.length() > 0) {
						s += ' ';
					}
					s += getOperator();
				}
				if (s.length() > 0) {
					s += ' ';
				}
				if (c instanceof NaryCriteria) {
					s += "(" + c + ")";
				} else {
					s += c;
				}
			}
		}
		return s;
	}
}
