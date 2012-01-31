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

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class Iteration extends CustomFieldValue {
	public Iteration(String value) {
		setValue(value);
	}

	public Iteration() {

	}

	private static final long serialVersionUID = 1L;

	// Override equals and hashcode to make it values based. See task 2833 for discussion
	@Override
	public int hashCode() {
		final int prime = 31;
		String value = getValue();
		int result = prime * ((value == null) ? super.hashCode() : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		AbstractReferenceValue other = (AbstractReferenceValue) obj;
		String value = getValue();
		String otherValue = other.getValue();
		if (value == null) {
			return otherValue == null;
		} else {
			return value.equals(otherValue);
		}
	}

}
