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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;

import java.util.List;


import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.web.shared.ProjectDashboard;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDashboardPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectDashboardView;

public class ProjectDashboardPresenter extends AbstractProfilePresenter implements SplittableActivity {

	private static final int UPDATE_PERIOD = 60 * 1000;

	private String projectId;
	private final ProjectDashboardView dashboardView;

	public ProjectDashboardPresenter(ProjectDashboardView view) {
		super(view);
		dashboardView = view;
	}

	public ProjectDashboardPresenter() {
		this(new ProjectDashboardView());
	}

	public void setPlace(Place p) {
		ProjectDashboardPlace place = (ProjectDashboardPlace) p;
		projectId = place.getProject().getIdentifier();
		fetchActivityData();
		fetchDashboardData();
	}

	private void fetchDashboardData() {
		final String fetchingProjectId = projectId;
		AppGinjector.get
				.instance()
				.getProfileService()
				.getDashboard(fetchingProjectId,
						new AsyncCallbackSupport<ProjectDashboard>(OperationMessage.create("Loading Dashboard")) {

							@Override
							protected void success(final ProjectDashboard result) {
								if (!stillOnPage(fetchingProjectId)) {
									return;
								}
								dashboardView.drawCommits(result.getCommitsByAuthor());
								dashboardView.drawTimelines(result.getTaskSummaries(), result.getScmSummaries());
								dashboardView.hudsonStatusView.setStatus(result.getBuildStatus());

								Timer timer = new Timer() {

									@Override
									public void run() {
										if (!stillOnPage(fetchingProjectId)) {
											return;
										}

										fetchDashboardData();
									}

								};
								timer.schedule(UPDATE_PERIOD);

							}
						});
	}

	private boolean stillOnPage(String projectId) {
		return this.projectId.equals(projectId) && dashboardView.isAttached() && dashboardView.isVisible();
	}

	private void fetchActivityData() {
		final String fetchingProjectId = projectId;
		AppGinjector.get
				.instance()
				.getProfileService()
				.getRecentActivity(fetchingProjectId,
						new AsyncCallbackSupport<List<ProjectActivity>>(OperationMessage.create("Loading Activity")) {

							@Override
							protected void success(List<ProjectActivity> result) {
								if (!stillOnPage(fetchingProjectId)) {
									return;
								}

								dashboardView.activityView.renderActivity(result);

								Timer timer = new Timer() {

									@Override
									public void run() {
										if (!stillOnPage(fetchingProjectId)) {
											return;
										}

										fetchActivityData();
									}

								};
								timer.schedule(UPDATE_PERIOD);
							}
						});
	}

	@Override
	protected void bind() {

	}

}
