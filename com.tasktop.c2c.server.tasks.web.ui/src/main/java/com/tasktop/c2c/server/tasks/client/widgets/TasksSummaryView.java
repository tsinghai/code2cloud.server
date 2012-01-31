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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.List;
import java.util.Map;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryPlace;
import com.tasktop.c2c.server.tasks.domain.Task;

public class TasksSummaryView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, TasksSummaryView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	private String appId;
	private Number productId;

	@UiField
	public FlowPanel taskSummaryPanel;
	@UiField
	public HeadingElement taskSummaryTitle;

	public TasksSummaryView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setHeader(String text) {
		taskSummaryTitle.setInnerText(text);
	}

	public void renderTaskSummaryForMap(Map<String, List<Task>> milestoneMap) {
		for (String milestone : milestoneMap.keySet()) {
			createTaskSegment(milestone, milestoneMap.get(milestone));
		}
	}

	public void renderTaskSummaryForList(List<Task> taskList) {
		createTaskSegment(null, taskList);
	}

	public void createTaskSegment(String segmentTitle, List<Task> segmentTaskList) {

		// This was originally implemented using a similar pattern to ApplicationCard - however, that way took several
		// seconds on large lists and this is basically instantaneous (even though it doesn't look as pretty).

		SafeHtmlBuilder bodyBuilder = new SafeHtmlBuilder();

		int numClosed = 0;

		for (Task curTask : segmentTaskList) {

			bodyBuilder.appendHtmlConstant("<div class=\"task-summary-card\">");

			Anchor link = new Anchor("Task " + curTask.getId());
			link.setHref(ProjectTaskPlace.createPlace(appId, curTask.getId()).getHref());

			if (!curTask.getStatus().isOpen()) {
				link.addStyleName("resolved");
				numClosed++;
			}

			bodyBuilder.appendHtmlConstant(link.toString());
			bodyBuilder.appendHtmlConstant(" -- ");
			bodyBuilder.appendEscaped(curTask.getShortDescription());
			bodyBuilder.appendHtmlConstant("<div class=\"clearleft\"></div></div>");

		}

		SafeHtmlBuilder headerBuilder = new SafeHtmlBuilder();

		if (segmentTitle != null) {
			int total = segmentTaskList.size();
			int numOpen = total - numClosed;
			Task sampleTask = segmentTaskList.get(0);

			Anchor link = new Anchor(segmentTitle, ProjectTasksSummaryPlace.createPlaceForComponentAndRelease(appId,
					(Integer) productId, sampleTask.getComponent().getId(), sampleTask.getMilestone().getValue())
					.getHref());

			// We have a title, so insert it at the beginning of the HTML.
			headerBuilder.appendHtmlConstant("<h3 class=\"task-summary-header\">");
			headerBuilder.appendHtmlConstant(link.toString());
			headerBuilder.appendHtmlConstant(" (" + total + " tasks in total: " + numOpen + " open, " + numClosed
					+ " closed)</h3>");
		}

		// Now, add in the body after the header.
		headerBuilder.append(bodyBuilder.toSafeHtml());

		taskSummaryPanel.add(new HTML(headerBuilder.toSafeHtml()));
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}

	/**
	 * @param productId
	 *            the productId to set
	 */
	public void setProductId(Number productId) {
		this.productId = productId;
	}

	/**
	 * 
	 */
	public void clear() {
		taskSummaryPanel.clear();
	}
}
