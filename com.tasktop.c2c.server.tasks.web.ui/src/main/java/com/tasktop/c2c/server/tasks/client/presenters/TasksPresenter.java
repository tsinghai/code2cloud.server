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
package com.tasktop.c2c.server.tasks.client.presenters;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TaskListView;
import com.tasktop.c2c.server.tasks.client.widgets.TaskSearchView;
import com.tasktop.c2c.server.tasks.client.widgets.TasksView;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.shared.QueryState;
import com.tasktop.c2c.server.tasks.shared.action.CreateQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.CreateQueryResult;
import com.tasktop.c2c.server.tasks.shared.action.DeleteQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteQueryResult;

public class TasksPresenter extends AbstractTaskPresenter implements ITaskListView.Presenter, SplittableActivity {

	private final TasksView view;
	private String projectIdentifier;
	private boolean isAnonymous = false;

	private TaskSearchPresenter searchPresenter;
	private TaskListPresenter taskListPresenter;
	private String currentCriteria;
	private SavedTaskQuery selectedQuery; // REVIEW is this not duplicated from queryState.savedQuery ?

	private QueryState queryState;
	private RepositoryConfiguration repositoryConfiguration;

	public TasksPresenter(TasksView view) {
		super(view);
		this.view = view;
		view.advancedSearchLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				showSearchConfigure();
			}
		});
		taskListPresenter = new TaskListPresenter(new TaskListView(), this);
		taskListPresenter.bind();
		TaskSearchView searchView = new TaskSearchView();
		searchView.setCancelClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				onCancelEditQuery();
			}
		});
		searchPresenter = new TaskSearchPresenter(searchView);

		setupTaskQuery();
	}

	public TasksPresenter() {
		this(new TasksView());
	}

	public void setPlace(Place p) {
		final ProjectTasksPlace ptPlace = (ProjectTasksPlace) p;
		this.projectIdentifier = ptPlace.getProjectId();
		this.repositoryConfiguration = ptPlace.getRepositoryConfiguration();
		this.searchPresenter.setState(projectIdentifier, repositoryConfiguration);
		taskListPresenter.setProjectIdentifier(projectIdentifier);
		this.queryState = ptPlace.getQueryState();

		this.isAnonymous = AuthenticationHelper.isAnonymous();
		view.setPresenter(this);

		setupPredefinedQueryLinks();
		setupSavedQueryLinks();
		showQuery();
	}

	@Override
	protected void bind() {
		//
	}

	private void setupTaskQuery() {
		view.taskSearchTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					performTaskQuery();
				}
			}
		});
		view.searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				performTaskQuery();
			}
		});
	}

	private void performTaskQuery() {
		String textQuery = view.taskSearchTextBox.getValue();
		if (textQuery != null) {
			textQuery = querySafeValue(textQuery.trim());
		}
		if (textQuery != null && textQuery.length() == 0) {
			return;
		}

		ProjectTasksPlace.createPlaceForTextQuery(projectIdentifier, textQuery).go();

	}

	private String querySafeValue(String text) {
		if (text == null) {
			return null;
		}
		// can't have '?' in our query string, see bug 1649
		return text.replace("?", "");
	}

	private void setupPredefinedQueryLinks() {
		view.predefinedQueryMenuPanel.clear();
		for (final PredefinedTaskQuery query : PredefinedTaskQuery.values()) {

			// If we have an anonymous user and this query requires a user, skip it.
			if (isAnonymous && query.isUserRequired()) {
				continue;
			}
			view.addQueryMenuItem(query);
		}
	}

	protected void setupSavedQueryLinks() {
		view.setSavedTaskQueries(repositoryConfiguration.getSavedTaskQueries());
	}

	private void showQuery() {

		switch (queryState.getQueryType()) {
		case Criteria:
			showCriteriaQuery(queryState.getQueryString());
			break;
		case Predefined:
			showQuery(queryState.getPredefinedQuery());
			break;
		case Saved:
			showQuery(queryState.getSavedQuery());
			break;
		case Text:
			showTextQuery(queryState.getQueryString());
			break;
		case Named:
		case Default:
			throw new IllegalStateException();
		}

		taskListPresenter.doSearch(queryState);
		taskListPresenter.show(getContentPanel());
	}

	private void showSearchConfigure() {
		searchPresenter.setEditQuery(null);
		searchPresenter.go(getContentPanel());
	}

	private void showQuery(PredefinedTaskQuery query) {
		view.setSelectedPredefinedQuery(query);
		view.taskSearchTextBox.setValue(null);
	}

	private void showQuery(SavedTaskQuery query) {
		this.selectedQuery = query;
		view.setSelectedSavedQuery(query);
		view.taskSearchTextBox.setValue(null);
	}

	private void showTextQuery(String text) {
		view.taskSearchTextBox.setValue(text);
		view.setSelectedPredefinedQuery(null); // clears all selections
		this.currentCriteria = computeTextQueryCriteria(text);
	}

	private String computeTextQueryCriteria(String text) {
		return new NaryCriteria(Operator.OR, new ColumnCriteria(TaskFieldConstants.DESCRIPTION_FIELD,
				Operator.STRING_CONTAINS, text), new ColumnCriteria(TaskFieldConstants.SUMMARY_FIELD,
				Operator.STRING_CONTAINS, text), new ColumnCriteria(TaskFieldConstants.COMMENT_FIELD,
				Operator.STRING_CONTAINS, text)).toQueryString();
	}

	private void showCriteriaQuery(String criteria) {
		this.currentCriteria = criteria;
		view.taskSearchTextBox.setValue(null);
		view.setSelectedPredefinedQuery(null); // clears all selections
	}

	/**
	 * the content panel in which nested views/presenters should exist
	 */
	public HasOneWidget getContentPanel() {
		return view.contentPanel;
	}

	@Override
	public void doSaveCurrentQuery(String newQueryName) {
		SavedTaskQuery newQuery = new SavedTaskQuery();
		newQuery.setName(newQueryName);
		newQuery.setQueryString(this.currentCriteria);

		getDispatchService().execute(new CreateQueryAction(projectIdentifier, newQuery),
				new AsyncCallbackSupport<CreateQueryResult>(new OperationMessage("Saving")) {

					@Override
					protected void success(CreateQueryResult actionResult) {
						SavedTaskQuery result = actionResult.get();
						repositoryConfiguration.getSavedTaskQueries().add(result);
						view.savedQueryMenuPanel.clear();
						setupSavedQueryLinks();
						doQuery(result);
						taskListPresenter.onQuerySaved();
					}
				});
	}

	@Override
	public void doEditSelectedQuery() {
		switch (this.queryState.getQueryType()) {
		case Saved:
			doEditQuery(this.selectedQuery);
			break;
		case Criteria:
			doEditQuery(this.queryState.getQueryString());
			break;
		default:
			break;
		}
	}

	/**
	 * @param queryString
	 */
	private void doEditQuery(String criteriaQueryString) {
		view.setSelectedSavedQuery(null);
		searchPresenter.setEditCriteria(CriteriaParser.parse(criteriaQueryString));
		searchPresenter.show(getContentPanel());
	}

	@Override
	public void doQuery(final PredefinedTaskQuery query) {
		ProjectTasksPlace.createPlaceForPredefinedQuery(projectIdentifier, query).go();
	}

	@Override
	public void doQuery(final SavedTaskQuery query) {
		ProjectTasksPlace.createPlaceForNamedQuery(projectIdentifier, query.getName()).go();
	}

	@Override
	public void doEditQuery(SavedTaskQuery query) {
		view.setSelectedSavedQuery(query);
		searchPresenter.setEditQuery(query);
		searchPresenter.show(getContentPanel());
	}

	@Override
	public void doDeleteQuery(final SavedTaskQuery query) {
		getDispatchService().execute(new DeleteQueryAction(projectIdentifier, query),
				new AsyncCallbackSupport<DeleteQueryResult>(new OperationMessage("Deleting")) {

					@Override
					protected void success(DeleteQueryResult result) {
						repositoryConfiguration.getSavedTaskQueries().remove(query);
						setupSavedQueryLinks();
						if (selectedQuery == query) {
							selectedQuery = null;
							view.setSelectedSavedQuery(null);
							doQuery(PredefinedTaskQuery.DEFAULT);
						}
					}
				});
	}

	private void onCancelEditQuery() {
		taskListPresenter.show(getContentPanel());
	}

	@Override
	public boolean canEditQuery() {
		switch (queryState.getQueryType()) {
		case Criteria:
			return true;
		case Predefined:
			return false;
		case Saved:
			// TODO When we intro public queries we must be smarter about this.
			return !AuthenticationHelper.isAnonymous();
		case Text:
		default:
			return false; // Can just search again
		}

	}

	@Override
	public boolean canCreateQuery() {
		return !AuthenticationHelper.isAnonymous();
	}

}
