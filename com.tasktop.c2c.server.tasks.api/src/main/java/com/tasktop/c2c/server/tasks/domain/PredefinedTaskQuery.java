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
 * A predefined query for use with the task service.
 * 
 */
public enum PredefinedTaskQuery {
	MINE("Assigned to me", true), RELATED("Related to me", true), RECENT("Recently changed", false), OPEN("Open tasks",
			false), ALL("All tasks", false);

	public static final PredefinedTaskQuery DEFAULT = RECENT;

	private final String label;
	private final boolean userRequired;

	private PredefinedTaskQuery(String label, boolean userRequired) {
		this.label = label;
		this.userRequired = userRequired;
	}

	/**
	 * returns the value of the given text, which must match a {@link #toString() string representation} of a predefined
	 * task query
	 * 
	 * @throws IllegalArgumentException
	 *             if the text does not match any value
	 */
	public static PredefinedTaskQuery valueOfString(String text) throws IllegalArgumentException {
		if (text != null) {
			for (PredefinedTaskQuery query : values()) {
				if (query.toString().equals(text)) {
					return query;
				}
			}
		}
		throw new IllegalArgumentException();
	}

	public String getLabel() {
		return label;
	}

	public boolean isUserRequired() {
		return userRequired;
	}

	/**
	 * Provide a nice representation of this value, suitable for use in an URI. Typically it's just the lower-case
	 * representation of the enum constant name.
	 * 
	 * @see #getLabel()
	 */
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
