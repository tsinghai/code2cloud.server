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
package com.tasktop.c2c.server.common.web.client.view;

import com.google.gwt.core.client.GWT;

/**
 * Utility for working with avatars.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class Avatar {
	public enum Size {
		// NOTE: changes to these sizes need a corresponding file images/default_avatarNN.png where NN is the size
		LARGE(80), MEDIUM(50), SMALL(25), MICRO(15);

		private final int size;

		private Size(int size) {
			this.size = size;
		}

		int getSize() {
			return size;
		}
	}

	private static final String[] alternateAvatarUrls;
	static {
		String[] alternates = new String[Size.values().length];
		String moduleBaseURL = GWT.getHostPageBaseURL();
		if (!moduleBaseURL.endsWith("/")) {
			moduleBaseURL = moduleBaseURL + '/';
		}
		for (Size size : Size.values()) {
			alternates[size.ordinal()] = moduleBaseURL + "images/default_avatar" + size.getSize() + ".png";
		}
		alternateAvatarUrls = alternates;
	}

	public static String computeAvatarUrl(String gravatarHash, Size size) {
		if (gravatarHash == null) {
			gravatarHash = "00000000000000000000000000000000";
		}
		return "https://secure.gravatar.com/avatar/" + gravatarHash + ".jpg?s=" + size.getSize() + "&d="
				+ alternateAvatarUrls[size.ordinal()];
	}

}
