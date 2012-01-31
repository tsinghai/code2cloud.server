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

import java.util.Map;
import java.util.Map.Entry;


import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.Color3D;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.PieChart;
import com.tasktop.c2c.server.profile.domain.project.Profile;

/**
 * A summary pie chart of open tasks by severity.
 */
public class CommitsByAuthorPieChart extends PieChart {

	public void draw(Map<Profile, Integer> commitsByAuthor) {
		if (commitsByAuthor == null || commitsByAuthor.isEmpty()) {
			return;
		}

		AbstractDataTable data = createData(commitsByAuthor);
		super.draw(data, createOptions());
	}

	private Options createOptions() {
		Options options = Options.create();
		options.setWidth(DashboardChartConstants.PIE_CHART_WIDTH);
		options.setHeight(DashboardChartConstants.PIE_CHART_HEIGHT);
		options.setLegend(DashboardChartConstants.PIE_CHART_LEGEND_POS);
		// options.setColors(DashboardChartConstants.COLOR_PALETTE);
		options.setColors(getPallete());
		// options.set("pieSliceText", "label");
		return options;
	}

	private Color3D[] getPallete() {
		Color3D[] result = new Color3D[DashboardChartConstants.COLOR_PALETTE.length];
		int i = 0;
		for (String color : DashboardChartConstants.COLOR_PALETTE) {
			Color3D c = Color3D.create();
			c.setShadeColor("black");
			c.setFaceColor(color);
			result[i++] = c;
		}
		return result;
	}

	private AbstractDataTable createData(Map<Profile, Integer> commitsByAuthor) {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.STRING, "Author");
		data.addColumn(ColumnType.NUMBER, "Commits");

		data.addRows(commitsByAuthor.size());
		int i = 0;
		for (Entry<Profile, Integer> entry : commitsByAuthor.entrySet()) {
			data.setValue(i, 0, entry.getKey().getEmail());
			data.setValue(i++, 1, entry.getValue());
		}
		return data;
	}
}
