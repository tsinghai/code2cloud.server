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
package com.tasktop.c2c.server.common.web.client.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A DSL for configuring styles
 */
public class Styles {
	private enum StyleClasses {
		NOWRAP
	}

	private Set<Enum<?>> styles = new HashSet<Enum<?>>(1);

	/**
	 * Add alignment to the
	 * 
	 * @param align
	 * @return
	 */
	public Styles align(Align align) {
		styles.add(align);
		return this;
	}

	public Styles nowrap() {
		styles.add(StyleClasses.NOWRAP);
		return this;
	}

	public List<String> styleNames() {
		List<String> styleNames = new ArrayList<String>(5);
		for (Enum<?> style : styles) {
			styleNames.add(style.name().toLowerCase());
		}
		return styleNames;
	}

	/**
	 * indicate if the styles contains the given style value
	 */
	public boolean hasStyle(Enum<?> value) {
		return styles.contains(value);
	}

	/**
	 * indicate if {@link #align(Align) align} is specified in this styles.
	 */
	public boolean hasAlign() {
		for (Align align : Align.values()) {
			if (styles.contains(align)) {
				return true;
			}
		}
		return false;
	}
}
