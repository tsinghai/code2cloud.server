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
package com.tasktop.c2c.server.hudson.plugin.buildlistener;

import hudson.model.Hudson;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dougm (Tasktop Technologies Inc.)
 * 
 */
public class Configuration {
	private String baseEventUrl;
	private String projectIdentifier;

	public void configureDefaults() {
		if (baseEventUrl == null) {
			baseEventUrl = "http://localhost:8888/api/event";
		}
		if (projectIdentifier == null) {
			// Try to parse out the projectIdentifier from the hudson url
			try {
				String hudsonUrl = Hudson.getInstance().getRootUrl();
				if (hudsonUrl != null) {
					URI rootUri = new URI(hudsonUrl);
					Matcher m = Pattern.compile("/s/([^/]+)/hudson/").matcher(rootUri.getPath());
					if (m.matches()) {
						projectIdentifier = m.group(1);
					}
				}

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (projectIdentifier == null) {
			projectIdentifier = "code2cloud"; // FIXME
		}
	}

	public String getBaseEventUrl() {
		return baseEventUrl;
	}

	public void setBaseEventUrl(String baseEventUrl) {
		this.baseEventUrl = baseEventUrl;
	}

	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public String toString() {
		return "baseEventUrl=" + getBaseEventUrl() + ", " + "productIdentifier=" + getProjectIdentifier();
	}
}
