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
package com.tasktop.c2c.server.common.web.client.navigation;

import java.util.HashMap;
import java.util.Map;

public class Args {

	private Map<String, String> nameToValue = new HashMap<String, String>();

	public void map(String name, String value) {
		nameToValue.put(name, value);
	}

	public Long getLong(String name) {
		String value = getString(name);
		return value == null || value.length() == 0 ? null : Long.parseLong(value);
	}

	public Integer getInteger(String name) {
		String value = getString(name);
		return value == null || value.length() == 0 ? null : Integer.parseInt(value);
	}

	public String getString(String name) {
		String value = nameToValue.get(name);
		if (value == null && !nameToValue.containsKey(name)) {
			return null;
		}
		return value;
	}

	public boolean hasValueForName(String name) {
		return nameToValue.containsKey(name);
	}
}
