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
import com.google.gwt.visualization.client.visualizations.ImageSparklineChart;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;

public class ActivitySparkTimeline extends ImageSparklineChart {
	public void draw(List<TaskSummary> summaries, List<ScmSummary> scmSummaries) {
		AbstractDataTable data = createData(summaries, scmSummaries);
		super.draw(data, createOptions());
	}

	private Options createOptions() {
		Options options = Options.create();
		options.setWidth(DashboardChartConstants.TIMELINE_WIDTH);
		options.setHeight(DashboardChartConstants.TIMELINE_HEIGHT * 2);
		options.setLabelPosition("left");

		options.setBackgroundColor("#FFFFFF00"); // white transparent. FIXME does not work
		options.setFill(true);
		options.setColors(DashboardChartConstants.COLOR_PALETTE);
		options.setFocusBorderColor("black");

		return options;
	}

	private AbstractDataTable createData(List<TaskSummary> summaries, List<ScmSummary> scmSummaries) {
		DataTable data = DataTable.create();

		if (summaries != null && !summaries.isEmpty()) {
			data.addColumn(ColumnType.NUMBER, "Open Tasks");
			data.addColumn(ColumnType.NUMBER, "Closed Tasks");
		}

		if (scmSummaries != null && !scmSummaries.isEmpty()) {
			data.addColumn(ColumnType.NUMBER, "Commits");
		}

		int numRows = summaries != null ? summaries.size() : scmSummaries != null ? scmSummaries.size() : 0;
		if (numRows > 0) {
			data.addRows(numRows);
		}

		int row = 0;
		int column = 0;

		if (summaries != null && !summaries.isEmpty()) {
			for (TaskSummary summary : summaries) {
				int sumOpen = 0;
				for (TaskSummaryItem item : summary.getItems()) {
					sumOpen += item.getOpenCount();
				}
				data.setValue(row, 0, sumOpen);
				int sumClosed = 0;
				column++;
				for (TaskSummaryItem item : summary.getItems()) {
					sumClosed += item.getClosedCount();
				}
				data.setValue(row, 1, sumClosed);
				column--;

				row++;
			}
			column = 2;
		}

		if (scmSummaries != null && !scmSummaries.isEmpty()) {
			row = 0;
			for (ScmSummary summary : scmSummaries) {
				data.setValue(row, column, summary.getAmount());
				row++;
			}
		}
		return data;
	}
}
