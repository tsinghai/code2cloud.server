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
package com.tasktop.c2c.server.profile.web.ui.client.graphs;

import com.google.gwt.visualization.client.LegendPosition;

public class DashboardChartConstants {

	public static final int TIMELINE_WIDTH = 320;
	public static final int TIMELINE_HEIGHT = 111;
	// From highest to lowest
	public static final String[] SEVERITY_COLORS = new String[] { "red", "orange", "green", "blue", "grey", "yellow",
			"violet" };

	public static final String[] COLOR_PALETTE = new String[] { "#8FBA5D", "#70C78D", "#458041", "#D1D15E", "#C7B85A" };

	public static final int POINT_SIZE = 0;
	public static final double TITLE_FONT_SIZE = 12;
	public static final double H_AXIS_FONT_SIZE = 5;

	public static final int PIE_CHART_WIDTH = 300;
	public static final int PIE_CHART_HEIGHT = 300;
	public static final LegendPosition PIE_CHART_LEGEND_POS = LegendPosition.NONE;
}
