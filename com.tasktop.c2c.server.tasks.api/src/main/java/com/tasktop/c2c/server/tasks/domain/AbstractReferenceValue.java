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

public class AbstractReferenceValue extends AbstractDomainObject implements Comparable<AbstractReferenceValue>,
		Serializable {

	private static final long serialVersionUID = 1L;
	private String value;
	private Short sortkey;

	/**
	 * The value, i.e., it's name.
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * An indication (for comparison) of how one value compares to another. Smaller values indicate more (should appear
	 * first).
	 */
	public Short getSortkey() {
		return sortkey;
	}

	public void setSortkey(Short sortkey) {
		this.sortkey = sortkey;
	}

	/**
	 * Provide the user-visible representation of the {@link #getValue()}. The default implementation simply returns the
	 * value.
	 */
	public String toString() {
		return getValue();
	}

	public int compareTo(AbstractReferenceValue o) {
		if (o == this) {
			return 0;
		}
		int i = sortkey - o.sortkey;
		if (i == 0) {
			i = value.compareTo(o.value);
		}
		return i;
	}

}
