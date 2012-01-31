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


import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverParameterInterpreter;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;

/**
 * A task summary bar graph
 */
public class TaskSummaryGraph extends GChart {

	private static final String openColor = "#006ca1";
	private static final String resolvedColor = "#6b915e";
	private static final Integer barWidth = 30;

	public TaskSummaryGraph(TaskSummary taskSummary) {

		setChartTitle("<b>Task Summary</b>");
		setChartSize(800, 150);
		setHoverParameterInterpreter(new TaskCountHoverParameterInterpreter());

		addCurve();
		Curve openTasksCurve = getCurve();
		Symbol symbol = openTasksCurve.getSymbol();
		symbol.setSymbolType(SymbolType.VBAR_SOUTHWEST);
		symbol.setBackgroundColor(openColor);
		symbol.setBorderColor(openColor);
		symbol.setWidth(barWidth);
		symbol.setHovertextTemplate(GChart.formatAsHovertext("#{open_task_count} open tasks"));
		symbol.setHovertextTemplate(GChart.formatAsHovertext("<b><tt>${open_task_count} open tasks</tt></b>"));

		addCurve();
		Curve resolvedTasksCurve = getCurve();
		Symbol resolvedSymbol = resolvedTasksCurve.getSymbol();
		resolvedSymbol.setSymbolType(SymbolType.VBAR_SOUTHEAST);
		resolvedSymbol.setBackgroundColor(resolvedColor);
		resolvedSymbol.setBorderColor(resolvedColor);
		resolvedSymbol.setWidth(barWidth);
		resolvedSymbol.setHovertextTemplate(GChart.formatAsHovertext("#{resolved_task_count} resolved tasks"));
		resolvedSymbol.setHovertextTemplate(GChart
				.formatAsHovertext("<b><tt>${resolved_task_count} resolved tasks</tt></b>"));

		// add ticks and data
		// FIXME: the first tick just pushes everything right
		openTasksCurve.addPoint(2, 0);
		getXAxis().addTick(2, "");

		List<TaskSummaryItem> taskSummaryItems = taskSummary.getItems();
		Integer length = taskSummaryItems.size();

		for (int i = 0; i < length; i++) {
			TaskSummaryItem taskSummaryItem = taskSummaryItems.get(i);
			String severity = taskSummaryItem.getSeverity().getValue();
			Integer openCount = taskSummaryItem.getOpenCount();
			Integer closedCount = taskSummaryItem.getClosedCount();

			getXAxis().addTick(2 + (length / 2 + i * (length + 1)), severity);

			openTasksCurve.addPoint(2 + (length / 2 + i * (length + 1)), openCount);

			resolvedTasksCurve.addPoint(2 + (length / 2 + i * (length + 1)), closedCount);
		}

		getXAxis().setAxisLabel("Severity");
		getYAxis().setAxisLabel("Count");

		// Set the range
		Double rangeMax = Math.ceil(getYAxis().getDataMax() / 100) * 100;
		getYAxis().setAxisMax(rangeMax);
		getYAxis().setAxisMin(0);
		getYAxis().setTickCount(6);
	}

	// supports use of ${n} in hovertext templates.
	class TaskCountHoverParameterInterpreter implements HoverParameterInterpreter {
		public String getHoverParameter(String paramName, GChart.Curve.Point hoveredOver) {
			Double x = hoveredOver.getY();
			Integer result = (int) Math.round(x);
			return result.toString();
		}
	}
}
