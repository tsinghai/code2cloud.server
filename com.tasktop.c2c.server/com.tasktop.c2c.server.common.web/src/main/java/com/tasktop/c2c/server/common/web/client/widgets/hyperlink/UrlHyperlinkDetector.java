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
package com.tasktop.c2c.server.common.web.client.widgets.hyperlink;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * 
 */
public class UrlHyperlinkDetector implements HyperlinkDetector {

	@Override
	public List<Hyperlink> detectHyperlinks(String text) {
		// GWT can't do regex find so instead we
		// have a simple detection algorithm
		// RFC 3986
		List<Hyperlink> hyperlinks = new ArrayList<Hyperlink>();

		for (int x = 0; x < text.length(); ++x) {
			if (x < (text.length() - 8)) {
				int protocolEndOffset = findProtocolEnd(text, x);
				if (protocolEndOffset > x) {
					int urlEnd = findUrlEnd(text, protocolEndOffset);
					hyperlinks.add(createHyperlink(text, x, urlEnd - x));
					x = urlEnd - 1;
				}
			}
		}
		return hyperlinks;
	}

	protected Hyperlink createHyperlink(String text, int offset, int length) {
		return new UrlHyperlink(text.substring(offset, offset + length), offset, length);
	}

	private int findUrlEnd(String text, int offset) {
		char previousChar = ' ';
		for (; offset < text.length(); ++offset) {
			char c = text.charAt(offset);
			if (!isValidUrlChar(c)) {
				if (previousChar == '.' || previousChar == ',' || previousChar == ';' || previousChar == ':'
						|| previousChar == ')') {
					--offset;
				}
				break;
			}
			previousChar = c;
		}
		return offset;
	}

	/**
	 * @param c
	 * @return
	 */
	private boolean isValidUrlChar(char c) {
		// see RFC 3986
		if (c >= 'a' && c <= 'z') {
			return true;
		}
		// ?@A-Z
		if (c >= '?' && c <= 'Z') {
			return true;
		}
		// #$%&'()*+,-./0-9:;
		if (c >= '#' && c <= ';') {
			return true;
		}
		if (c == '_' || c == '~' || c == '=' || c == '!') {
			return true;
		}
		return false;
	}

	private int findProtocolEnd(String text, int offset) {
		char c = text.charAt(offset);
		if (c == 'h') {
			c = text.charAt(++offset);
			if (c == 't') {
				c = text.charAt(++offset);
				if (c == 't') {
					c = text.charAt(++offset);
					if (c == 'p') {
						c = text.charAt(++offset);
						if (c == ':') {
							return offset;
						} else if (c == 's') {
							c = text.charAt(++offset);
							if (c == ':') {
								return offset;
							}
						}
					}
				}
			}
		}
		return -1;
	}

}
