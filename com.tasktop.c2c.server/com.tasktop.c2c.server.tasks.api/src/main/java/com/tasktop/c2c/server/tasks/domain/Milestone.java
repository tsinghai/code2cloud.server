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

public class Milestone extends AbstractReferenceValue implements Serializable {
	private static final long serialVersionUID = 1L;
	private Product product;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Override
	public String toString() {
		String str = getValue();
		if (str == null || str.trim().length() == 0) {
			// If there's no name, then still return a string - if we don't do this, then Spring's validation system
			// blows up since it does object.toString(), so even if there's a valid non-null value in a field we still
			// get a validation failure.
			return "no name, ID: " + getId();
		} else {
			return str;
		}
	}

	@Override
	public String getValue() {
		return super.getValue();
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

	@Override
	public Short getSortkey() {
		return super.getSortkey();
	}

	@Override
	public void setSortkey(Short sortkey) {
		super.setSortkey(sortkey);
	}
}
