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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.web.ui.client.widgets.build.BuildResources;

public class HudsonStatusView extends Composite {
	interface Binder extends UiBinder<Widget, HudsonStatusView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	public static BuildResources buildResources = GWT.create(BuildResources.class);

	public HudsonStatusView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Panel jobSummaryPanel;

	public void setStatus(HudsonStatus status) {
		jobSummaryPanel.clear();
		if (status == null) {
			return;
		}
		setJobs(status.getJobs());
	}

	private void setJobs(List<JobSummary> jobs) {
		Grid table = new Grid(jobs.size(), 2);
		for (int i = 0; i < jobs.size(); i++) {
			JobSummary job = jobs.get(i);
			table.setWidget(i, 0, getWidgetFromColour(job.getColor()));
			table.setWidget(i, 1, new Anchor(job.getName(), job.getUrl()));
		}
		jobSummaryPanel.add(table);
	}

	private Widget getWidgetFromColour(String color) {
		if (color.equals("blue")) {
			return new Image(buildResources.stableBuild());
		} else if (color.equals("red")) {
			return new Image(buildResources.failedBuild());
		} else if (color.equals("yellow")) {
			return new Image(buildResources.unstableBuild());
		} else if (color.equals("grey")) {
			return new Image(buildResources.canceledBuild());
		} else if (color.equals("aborted")) {
			return new Image(buildResources.canceledBuild());
		} else if (color.equals("disabled")) {
			return new Image(buildResources.disabledBuild());
		} else if (color.equals("blue_anime")) {
			return new Image(buildResources.stableBuilding());
		} else if (color.equals("red_anime")) {
			return new Image(buildResources.failedBuilding());
		} else if (color.equals("yellow_anime")) {
			return new Image(buildResources.unstableBuilding());
		} else if (color.equals("grey_anime")) {
			return new Image(buildResources.canceledBuilding());
		} else if (color.equals("aborted_anime")) {
			return new Image(buildResources.canceledBuilding());
		} else {
			return new Label(color);
		}
	}
}
