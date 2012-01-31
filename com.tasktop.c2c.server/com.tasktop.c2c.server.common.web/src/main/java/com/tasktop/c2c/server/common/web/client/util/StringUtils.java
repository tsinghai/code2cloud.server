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
package com.tasktop.c2c.server.common.web.client.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class StringUtils {

	private StringUtils() {
		// No instantiation of this class
	}

	public static final boolean hasText(String str) {
		// If our string is not just whitespace, return true.
		return (str != null && str.trim().length() > 0);
	}

	public static String concatenate(Collection<?> coll) {
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

}
