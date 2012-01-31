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
package com.tasktop.c2c.server.common.service.wiki;

import java.io.Writer;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class WebSafeHtmlDocumentBuilder extends HtmlDocumentBuilder {

	protected static final Pattern PROTOCOL_PATTERN = Pattern.compile("((https?://)|(mailto:)|(#)).*",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("\\s*javascript:.*", Pattern.CASE_INSENSITIVE);
	private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
			"<((script|link|iframe)|[a-zA-Z][a-zA-Z0-9]*.*?on[A-Z][a-zA-Z]+=)", Pattern.CASE_INSENSITIVE);
	private String pagePrefix;

	public WebSafeHtmlDocumentBuilder(Writer out) {
		this(out, null);
	}

	public WebSafeHtmlDocumentBuilder(Writer out, String pagePrefix) {
		super(out);
		this.pagePrefix = pagePrefix;

		// By default, don't emit as document.
		this.setEmitAsDocument(false);
	}

	public void setPagePrefix(String newPagePrefix) {
		this.pagePrefix = newPagePrefix;
	}

	@Override
	protected String makeUrlAbsolute(String url) {
		if (!PROTOCOL_PATTERN.matcher(url).matches()) {
			return pagePrefix + url;
		} else if (JAVASCRIPT_PATTERN.matcher(url).matches()) {
			return ""; // ignore javascript
		}
		return super.makeUrlAbsolute(url);
	}

	@Override
	public void charactersUnescaped(String literal) {
		if (SCRIPT_TAG_PATTERN.matcher(literal).find()) {
			characters(literal);
		} else {
			super.charactersUnescaped(literal);
		}
	}
}
