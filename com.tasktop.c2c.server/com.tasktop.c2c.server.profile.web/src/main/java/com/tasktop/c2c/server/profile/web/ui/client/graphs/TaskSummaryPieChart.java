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


import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.PieChart;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;

/**
 * A summary pie chart of open tasks by severity.
 */
public class TaskSummaryPieChart extends PieChart {

	public void draw(TaskSummary taskSummary) {
		AbstractDataTable data = createData(taskSummary);
		super.draw(data, createOptions());
	}

	private Options createOptions() {
		Options options = Options.create();
		options.setWidth(DashboardChartConstants.PIE_CHART_WIDTH);
		options.setHeight(DashboardChartConstants.PIE_CHART_HEIGHT);
		options.setColors(DashboardChartConstants.SEVERITY_COLORS);
		options.setLegend(DashboardChartConstants.PIE_CHART_LEGEND_POS);
		// options.set("pieSliceText", "label");
		return options;
	}

	private AbstractDataTable createData(TaskSummary taskSummary) {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.STRING, "Severity");
		data.addColumn(ColumnType.NUMBER, "Total");

		data.addRows(taskSummary.getItems().size());
		int i = 0;
		for (TaskSummaryItem item : taskSummary.getItems()) {
			data.setValue(i, 0, item.getSeverity().getValue());
			data.setValue(i++, 1, item.getOpenCount());
		}
		return data;
	}
}
