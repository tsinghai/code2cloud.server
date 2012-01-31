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
package com.tasktop.c2c.server.common.web.client.widgets.chooser;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractValueSuggestion<T> implements ValueSuggestion<T> {

	private T value;

	public AbstractValueSuggestion(T value) {
		this.value = value;
	}

	@Override
	public abstract String getDisplayString();

	@Override
	public String getReplacementString() {
		// IMPORTANT: replacement string is always empty, to prevent
		// suggestion list from appearing after keyup on <return> key.
		return "";
	}

	public T getValue() {
		return value;
	}
}
