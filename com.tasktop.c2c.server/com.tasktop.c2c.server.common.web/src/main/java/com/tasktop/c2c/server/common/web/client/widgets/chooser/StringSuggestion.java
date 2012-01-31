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
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public class StringSuggestion extends AbstractValueSuggestion<String> {

	/**
	 * @param value
	 */
	public StringSuggestion(String value) {
		super(value);
	}

	@Override
	public String getDisplayString() {
		return getValue();
	}

}
