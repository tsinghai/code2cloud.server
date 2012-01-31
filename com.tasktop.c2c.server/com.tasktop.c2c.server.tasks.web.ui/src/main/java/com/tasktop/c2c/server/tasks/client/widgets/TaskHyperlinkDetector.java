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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.Hyperlink;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.HyperlinkDetector;

/**
 * A hyperlink detector for tasks
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class TaskHyperlinkDetector implements HyperlinkDetector {

	private String projectIdentity;

	public TaskHyperlinkDetector(String projectIdenitty) {
		this.projectIdentity = projectIdenitty;
	}

	@Override
	public List<Hyperlink> detectHyperlinks(String text) {
		List<Hyperlink> hyperlinks = new ArrayList<Hyperlink>();

		char[][] prefixes = new char[][] { { 't', 'a', 's', 'k' }, { 'b', 'u', 'g' }, };

		final int last = text.length() - 5;
		for (int x = 0; x < last; ++x) {
			for (char[] prefix : prefixes) {
				if (matches(text, x, prefix)) {
					int end = eatSeparator(text, x + prefix.length);
					int digitsEnd = eatDigits(text, end);
					if (digitsEnd > end) {
						hyperlinks.add(new TaskHyperlink(projectIdentity, text.substring(end, digitsEnd), text
								.substring(x, digitsEnd), x, digitsEnd - x));
					}
					x = end - 1;
					break;
				}
			}
		}

		return hyperlinks;
	}

	private boolean matches(String text, int offset, char[] chars) {
		int max = offset + chars.length;
		if (text.length() > max) {
			for (int x = 0; x < chars.length; ++x) {
				if (text.charAt(offset + x) != chars[x]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private int eatDigits(String text, int offset) {
		if (offset < text.length()) {
			char c = text.charAt(offset);
			while ((c >= '0' && c <= '9') && offset < text.length()) {
				++offset;
				if (offset == text.length()) {
					break;
				}
				c = text.charAt(offset);
			}
		}
		return offset;
	}

	private int eatSeparator(String text, int offset) {
		if (offset < text.length()) {
			char c = text.charAt(offset);
			while ((c == ' ' || c == '#') && offset < text.length() - 1) {
				++offset;
				if (offset == text.length()) {
					break;
				}
				c = text.charAt(offset);
			}
		}
		return offset;
	}

}
