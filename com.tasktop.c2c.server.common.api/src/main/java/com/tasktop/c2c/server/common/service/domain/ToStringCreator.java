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

/**
 * Utility class used for custom toString methods. Modeled after spring's util of the same name, but needed here to
 * maintain gwt compliance with our DOs.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class ToStringCreator {
	private StringBuilder builder = new StringBuilder();
	private Object instance;
	private boolean needSep = false;

	public ToStringCreator(Object instance) {
		this.instance = instance;
		builder.append(getSimpleName(instance.getClass()));
		builder.append("{");
	}

	private String getSimpleName(Class<?> c) {
		String fullName = c.getName();
		int lastDot = fullName.lastIndexOf(".");
		if (lastDot == -1) {
			return fullName;
		} else {
			return fullName.substring(lastDot + 1);
		}
	}

	public ToStringCreator append(String fieldName, Object field) {
		if (needSep) {
			builder.append(", ");
		} else {
			needSep = true;
		}

		builder.append(fieldName).append(": ");
		if (field == null) {
			builder.append("null");
		} else {
			builder.append(field.toString());
		}
		return this;
	}

	@Override
	public String toString() {
		builder.append("}");
		return builder.toString();
	}

}
