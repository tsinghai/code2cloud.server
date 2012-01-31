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
package com.tasktop.c2c.server.profile.web.ui.client.widgets.build;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface BuildResources extends ClientBundle {
	@Source("resources/stable-build-icon.gif")
	ImageResource stableBuild();

	@Source("resources/failed-build-icon.gif")
	ImageResource failedBuild();

	@Source("resources/unstable-build-icon.gif")
	ImageResource unstableBuild();

	@Source("resources/canceled-build-icon.gif")
	ImageResource canceledBuild();

	@Source("resources/disabled-build-icon.gif")
	ImageResource disabledBuild();

	@Source("resources/stable-building-icon.gif")
	ImageResource stableBuilding();

	@Source("resources/failed-building-icon.gif")
	ImageResource failedBuilding();

	@Source("resources/unstable-building-icon.gif")
	ImageResource unstableBuilding();

	@Source("resources/canceled-building-icon.gif")
	ImageResource canceledBuilding();
}
