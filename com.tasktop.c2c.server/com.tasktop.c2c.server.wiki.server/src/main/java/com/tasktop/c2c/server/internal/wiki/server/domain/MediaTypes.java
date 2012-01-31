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

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class MediaTypes {

	// Blacklisted media types
	private Set<MediaType> unSupportedMediaTypes = new HashSet<MediaType>();
	{
		unSupportedMediaTypes.addAll(MediaType
				.parseMediaTypes("text/html, text/thm, text/javascript, application/js, application/javascript, "
						+ "application/js, application/jsb, application/x-javascript"));
		unSupportedMediaTypes
				.addAll(MediaType
						.parseMediaTypes("application/mhtml, application/mht, application/shtml, application/dll, application/src, "
								+ "application/msi, application/vbs, application/bat, application/com, application/pif, application/cmd, "
								+ "application/vxb, application/cpl, application/x-msdownload, application/exe, application/pl, application/py, application/cgi"));
	}

	public boolean isSupported(MediaType mediaType) {
		boolean unSupported = unSupportedMediaTypes.contains(mediaType);
		if (!unSupported) {
			for (MediaType type : unSupportedMediaTypes) {
				if (type.includes(mediaType)) {
					unSupported = true;
					break;
				}
			}
		}
		return !unSupported;
	}

}
