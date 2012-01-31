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
import java.util.Map;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;
import com.tasktop.c2c.server.profile.web.ui.client.GoogleVisLoader;
import com.tasktop.c2c.server.profile.web.ui.client.graphs.ActivitySparkTimeline;
import com.tasktop.c2c.server.profile.web.ui.client.graphs.CommitsByAuthorPieChart;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;

public class ProjectDashboardView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, ProjectDashboardView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public HudsonStatusView hudsonStatusView;
	@UiField
	public BaseActivityView activityView;
	@UiField
	Panel commitsByAuthorPanel;
	private CommitsByAuthorPieChart commitsByAuthorPieChart;

	@UiField
	Panel activityTimelinePanel;
	private ActivitySparkTimeline activitySparkTimeline;

	public ProjectDashboardView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void drawCommits(final Map<Profile, Integer> commitsByAuthor) {
		GoogleVisLoader.ensureGoogleVisLoaded(new Runnable() {
			@Override
			public void run() {
				if (commitsByAuthorPieChart == null) {
					commitsByAuthorPieChart = new CommitsByAuthorPieChart();
					commitsByAuthorPanel.add(commitsByAuthorPieChart);
				}

				commitsByAuthorPieChart.draw(commitsByAuthor);
			}
		});
	}

	public void drawTimelines(final List<TaskSummary> tasks, final List<ScmSummary> scm) {
		GoogleVisLoader.ensureGoogleVisLoaded(new Runnable() {
			@Override
			public void run() {
				if (activitySparkTimeline == null) {
					activitySparkTimeline = new ActivitySparkTimeline();
					activityTimelinePanel.add(activitySparkTimeline);
				}
				activitySparkTimeline.draw(tasks, scm);
			}
		});

	}

}
