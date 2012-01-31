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
package com.tasktop.c2c.server.internal.wiki.server.domain;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;

import com.tasktop.c2c.server.common.service.wiki.WebSafeHtmlDocumentBuilder;
import com.tasktop.c2c.server.internal.wiki.server.WikiServiceConfiguration;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class WikiHtmlDocumentBuilder extends WebSafeHtmlDocumentBuilder {

	public static final char HASHTAG_DELIMITER = '-';
	private static final String PROJECT_PREFIX = "#projects/";

	private final Page page;
	private final String pageHashtag;
	private final WikiServiceConfiguration serviceConfiguration;

	/**
	 * @param out
	 * @param pagePrefix
	 */
	public WikiHtmlDocumentBuilder(Writer out, Page relatedPage, WikiServiceConfiguration serviceConfig) {
		super(out);
		this.page = relatedPage;
		this.serviceConfiguration = serviceConfig;
		String pagePrefix = serviceConfiguration.computePathForPage("");
		setPagePrefix(pagePrefix);

		// To create our hashtag, append the current page's path onto the page prefix.
		this.pageHashtag = pagePrefix + encode(page.getPath());
	}

	@Override
	protected String makeUrlAbsolute(String url) {
		if (url.startsWith("#")) {
			if (url.startsWith(PROJECT_PREFIX)) {
				// assume full hashtag
				return url;
			} else {
				// handle in-page linking
				return pageHashtag + HASHTAG_DELIMITER + url.substring(1);
			}
		}
		return super.makeUrlAbsolute(url);
	}

	@Override
	public void image(Attributes attributes, String imageUrl) {
		imageUrl = computeWikiImageUrl(imageUrl);
		super.image(attributes, imageUrl);
	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		imageUrl = computeWikiImageUrl(imageUrl);
		super.imageLink(linkAttributes, imageAttributes, href, imageUrl);
	}

	private String computeWikiImageUrl(String imageUrl) {
		if (!WebSafeHtmlDocumentBuilder.PROTOCOL_PATTERN.matcher(imageUrl).matches() && imageUrl.indexOf('/') == -1) {
			return serviceConfiguration.computeWebUrlForAttachment(page.getId(), imageUrl);
		}
		return imageUrl;
	}

	private String encode(String path) {
		try {
			return URLEncoder.encode(path, "utf-8").replace("%20", "+").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
