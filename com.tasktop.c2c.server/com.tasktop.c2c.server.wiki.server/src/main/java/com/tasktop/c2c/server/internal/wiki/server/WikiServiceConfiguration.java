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
package com.tasktop.c2c.server.internal.wiki.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.web.util.UriUtils;

import com.tasktop.c2c.server.common.service.BaseProfileConfiguration;

public class WikiServiceConfiguration extends BaseProfileConfiguration {

	public String getProfileProjectIdentifier() {
		return (String) TenancyContextHolder.getContext().getTenant().getIdentity();
	}

	public String computeWebUrlForPage(String pagePath) {

		return getProfileBaseUrl() + "/" + computePathForPage(pagePath);
	}

	public String computePathForPage(String pagePath) {
		try {
			return "#projects/" + getProfileProjectIdentifier() + "/wiki/p/"
					+ URLEncoder.encode(pagePath, "utf-8").replace("%20", "+").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}

	public String computeWebUrlForAttachment(Long id, String name) {
		try {
			String pathString = String.format("%swiki/%s/attachment/%s",
					getServiceUrlPrefix(getProfileProjectIdentifier()), id, name);
			return UriUtils.encodeHttpUrl(pathString, "utf-8");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public String computeAttachmentsUrlForPage(Long id) {
		return getServiceUrlPrefix(getProfileProjectIdentifier()) + "wiki/" + id + "/attachment";
	}
}
