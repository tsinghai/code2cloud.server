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
package com.tasktop.c2c.server.common.web.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;

/**
 * An URL encoder that delegates to {@link URL GWT URL} when {@link GWT#isClient() in client mode}. Safe to use when
 * unit testing in Java.
 */
public class URLEncoding {

	private static final char[] chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };

	public static String encode(String part) {
		return encode(part, null);
	}

	/**
	 * encode the given URI part.When {@link GWT#isClient() is client = true}, GWT client URL encoding is used.
	 */
	public static String encode(String part, char[] excludes) {
		if (GWT.isClient()) {
			return URL.encode(part).replace("%20", "+");
		}
		StringBuilder result = new StringBuilder(part.length() + 10);
		for (int x = 0; x < part.length(); ++x) {
			// see http://en.wikipedia.org/wiki/Percent-encoding
			char c = part.charAt(x);
			if (isUnreserved(c) || excluded(c, excludes)) {
				result.append(c);
			} else {
				if (c == ' ') {
					result.append('+');
				} else {
					result.append('%');
					result.append(chars[c >> 4]);
					result.append(chars[c & 0x0F]);
				}
			}
		}
		return result.toString();
	}

	private static boolean excluded(char c, char[] excludes) {
		if (excludes == null) {
			return false;
		}
		for (int x = 0; x < excludes.length; ++x) {
			if (excludes[x] == c) {
				return true;
			}
		}
		return false;
	}

	/**
	 * decode the given URI part. When {@link GWT#isClient() is client = true}, GWT client URL decoding is used.
	 */
	public static String decode(String part) {
		if (GWT.isClient()) {
			return URL.decode(part.replace("+", "%20"));
		}
		StringBuilder result = new StringBuilder(part.length() + 10);
		for (int x = 0; x < part.length(); ++x) {
			// see http://en.wikipedia.org/wiki/Percent-encoding
			char c = part.charAt(x);
			if (isUnreserved(c)) {
				result.append(c);
			} else {
				if (c == '+') {
					c = ' ';
				} else if (c == '%') {
					c = (char) Integer.parseInt("" + part.charAt(++x) + part.charAt(++x), 16);
				}
				result.append(c);
			}
		}
		return result.toString();
	}

	private static boolean isUnreserved(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-'
				|| c == '~' || c == '.';
	}

}
