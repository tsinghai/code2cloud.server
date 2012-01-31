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

import com.google.gwt.place.shared.Place;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectDiscoveryView;

public class ProjectDiscoveryPresenter extends AbstractProfilePresenter implements IProjectDiscoryView.Presenter,
		SplittableActivity {

	private QueryResult<Project> currentResult;
	private QueryRequest currentQueryRequest = new QueryRequest(new Region(0, 25), null);
	private String currentQueryString = null;
	private ProjectRelationship currentRelationship;

	private AsyncDataProvider<Project> viewAdapter;

	public ProjectDiscoveryPresenter(ProjectDiscoveryView view) {
		super(view);
		initializeViewAdapter();
	}

	public ProjectDiscoveryPresenter() {
		this(ProjectDiscoveryView.getInstance());
	}

	public void setPlace(Place p) {
		currentQueryString = ((ProjectsDiscoverPlace) p).getQuery();
		if (currentQueryString == null) {
			currentRelationship = ProjectRelationship.ALL;
		} else {
			currentRelationship = null;
		}
		ProjectDiscoveryView.getInstance().pager.setPageSize(currentQueryRequest.getPageInfo().getSize());
		ProjectDiscoveryView.getInstance().setPresenter(ProjectDiscoveryPresenter.this);
		update();
	}

	@Override
	protected void bind() {

	}

	private void update() {
		if (currentRelationship != null) {
			getProfileService().getProjects(currentRelationship, currentQueryRequest,
					new AsyncCallbackSupport<QueryResult<Project>>() {

						@Override
						protected void success(QueryResult<Project> result) {
							currentResult = result;
							viewAdapter.updateRowCount(result.getTotalResultSize(), true);
							viewAdapter.updateRowData(result.getOffset(), result.getResultPage());
						}
					});
		} else if (currentQueryString != null) {
			getProfileService().findProjects(currentQueryString, currentQueryRequest,
					new AsyncCallbackSupport<QueryResult<Project>>() {

						@Override
						protected void success(QueryResult<Project> result) {
							currentResult = result;
							viewAdapter.updateRowCount(result.getTotalResultSize(), true);
							viewAdapter.updateRowData(result.getOffset(), result.getResultPage());
						}
					});
		}
	}

	@Override
	public QueryResult<Project> getCurrentResult() {
		return currentResult;
	}

	@Override
	public void setProjectRelationship(ProjectRelationship projectRelationship) {
		this.currentQueryString = null;
		this.currentRelationship = projectRelationship;
		currentQueryRequest.getPageInfo().setOffset(0);
		ProjectDiscoveryView.getInstance().pager.setPageStart(0);
		update();
	}

	@Override
	public ProjectRelationship getProjectRelationship() {
		return currentRelationship;
	}

	@Override
	public QueryRequest getQueryRequest() {
		return currentQueryRequest;
	}

	@Override
	public void setQueryRequest(QueryRequest queryRequest) {
		this.currentQueryRequest = queryRequest;
		update();
	}

	private void initializeViewAdapter() {
		viewAdapter = new AsyncDataProvider<Project>() {
			@Override
			protected void onRangeChanged(HasData<Project> view) {
				currentQueryRequest.getPageInfo().setOffset(view.getVisibleRange().getStart());
				currentQueryRequest.getPageInfo().setSize(view.getVisibleRange().getLength());
				update();
			}
		};
		viewAdapter.addDataDisplay(ProjectDiscoveryView.getInstance().getProjectsDisplay());

	}

	@Override
	public String getCurrentQuery() {
		return currentQueryString;
	}
}
