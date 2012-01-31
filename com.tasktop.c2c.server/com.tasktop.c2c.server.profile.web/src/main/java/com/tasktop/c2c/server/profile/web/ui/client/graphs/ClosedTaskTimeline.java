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

import java.util.List;


import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;

public class ClosedTaskTimeline extends AreaChart {
	public void draw(List<TaskSummary> summaries) {
		AbstractDataTable data = createData(summaries);
		super.draw(data, createOptions());
	}

	private Options createOptions() {
		Options options = Options.create();
		options.setWidth(DashboardChartConstants.TIMELINE_WIDTH);
		options.setHeight(DashboardChartConstants.TIMELINE_HEIGHT);
		options.setLegend(LegendPosition.NONE);
		options.setTitle("Closed Tasks");
		options.setPointSize(DashboardChartConstants.POINT_SIZE);
		return options;
	}

	private AbstractDataTable createData(List<TaskSummary> summaries) {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATE, "Day");
		data.addColumn(ColumnType.NUMBER, "Closed Tasks");

		data.addRows(summaries.size());
		int row = 0;
		for (TaskSummary summary : summaries) {
			int sumClosed = 0;
			data.setValue(row, 0, summary.getDate());
			for (TaskSummaryItem item : summary.getItems()) {
				sumClosed += item.getClosedCount();
			}
			data.setValue(row, 1, sumClosed);
			row++;
		}
		return data;
	}
}
