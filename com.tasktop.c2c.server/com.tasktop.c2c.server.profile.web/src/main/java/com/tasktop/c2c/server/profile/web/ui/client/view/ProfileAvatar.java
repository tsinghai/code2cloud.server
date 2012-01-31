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
package com.tasktop.c2c.server.profile.web.ui.client.view;


import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.common.web.client.view.Avatar.Size;
import com.tasktop.c2c.server.profile.web.shared.Profile;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class ProfileAvatar {
	/**
	 * Compute the Avatar image URL. Prefer using {@link #computeAvatar(Profile)} instead, for consistent HTML.
	 */
	public static String computeAvatarUrl(Profile profile, Size size) {
		String gravatarHash = profile.getGravatarHash();
		return Avatar.computeAvatarUrl(gravatarHash, size);
	}

	public static String computeAvatarUrl(com.tasktop.c2c.server.profile.domain.project.Profile profile, Size size) {
		String gravatarHash = profile.getGravatarHash();
		return Avatar.computeAvatarUrl(gravatarHash, size);
	}
}
