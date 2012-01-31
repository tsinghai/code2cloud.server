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
package com.tasktop.c2c.server.profile.web.ui.client;

import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.ImageSparklineChart;

public class GoogleVisLoader {
	private static boolean loaded = false;

	public static void ensureGoogleVisLoaded(final Runnable afterLoad) {
		if (loaded) {
			afterLoad.run();
			return;
		}

		VisualizationUtils.loadVisualizationApi(new Runnable() {
			@Override
			public void run() {
				loaded = true;
				afterLoad.run();
			}
		}, "corechart", ImageSparklineChart.PACKAGE);
	}
}
