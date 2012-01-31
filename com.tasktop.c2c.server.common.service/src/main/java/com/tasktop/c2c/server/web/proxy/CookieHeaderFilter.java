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
package com.tasktop.c2c.server.web.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieHeaderFilter extends HeaderFilter {

	private static final Pattern COOKIE_VALUE_PATTERN = Pattern.compile("([^=]+)(=.+)");

	private static final String COOKIE_NAME_PREFIX = "ALMP.".toLowerCase();

	public String processRequestHeader(String headerName, String headerValue) {
		final boolean isCookie = headerName.equalsIgnoreCase("cookie");
		if (isCookie) {
			return filterCookie(headerName, headerValue);
		} else {
			return super.processRequestHeader(headerName, headerValue);
		}
	}

	public String processResponseHeader(String headerName, String headerValue) {
		if (headerName.equalsIgnoreCase("Set-Cookie") || headerName.equalsIgnoreCase("Set-Cookie2")) {
			return filterSetCookie(headerName, headerValue);
		} else {
			return super.processResponseHeader(headerName, headerValue);
		}
	}

	protected String filterSetCookie(String headerName, String headerValue) {
		// Mapping of cookie names: prefix proxy cookie names to avoid conflicts
		Matcher matcher = COOKIE_VALUE_PATTERN.matcher(headerValue);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String suffix = matcher.group(2);
			return COOKIE_NAME_PREFIX + name + suffix;
		} else {
			// drop the set-cookie if the value is malformed
			return null;
		}
	}

	String filterCookie(String headerName, String headerValue) {
		Matcher matcher = Pattern.compile("(\\$?[^\\s=\"]+)=((\"?).*?\\3)(?:;|,|(?:\\s*$))").matcher(headerValue);

		int cookieCount = 0;
		List<String> cookieTokens = null;
		boolean capturingTokens = true;
		while (matcher.find()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			boolean capture = false;
			if (name.toLowerCase().startsWith(COOKIE_NAME_PREFIX)) {
				capturingTokens = true;
				capture = true;
				++cookieCount;
				name = name.substring(COOKIE_NAME_PREFIX.length());
			} else if (name.startsWith("$") && capturingTokens) {
				capture = true;
			} else {
				capturingTokens = false;
			}
			if (capture) {
				if (cookieTokens == null) {
					cookieTokens = new ArrayList<String>(6);
				}
				cookieTokens.add(name + '=' + value);
			}
		}
		if (cookieCount > 0) {
			String newHeaderValue = "";
			for (String token : cookieTokens) {
				if (newHeaderValue.length() > 0) {
					newHeaderValue += ' ';
				}
				newHeaderValue += token;
				newHeaderValue += ';';
			}
			return newHeaderValue;
		}
		return null;
	}

}
