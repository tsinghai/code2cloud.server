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

import java.util.Collections;
import java.util.List;
import java.util.Map;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.web.client.navigation.Navigation;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.Message.MessageType;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.CellTableResources;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.ProjectNewTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TaskListView;
import com.tasktop.c2c.server.tasks.client.widgets.tasklist.TaskColumnDescriptor;
import com.tasktop.c2c.server.tasks.client.widgets.tasklist.TaskListColumnConfiguration;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.shared.QueryState;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryResult;

/**
 * a presenter for showing a list of tasks, usually used to show task search results.
 */
public class TaskListPresenter extends AbstractTaskPresenter {

	private final TaskListView taskListView;

	private CellTable<Task> resultsTable;
	private AsyncDataProvider<Task> dataProvider;
	private MultiSelectionModel<Task> selectionModel;
	private QueryState queryState;

	private String taskServiceBaseUrl;

	private TaskListColumnConfiguration taskListColumnConfiguration;

	public TaskListPresenter(TaskListView view, TasksPresenter parentPresenter) {
		super(view);
		taskListView = view;
		view.setPresenter(parentPresenter);
	}

	@Override
	public void setProjectIdentifier(String projectId) {
		super.setProjectIdentifier(projectId);
		String path = Window.Location.getPath();
		String appCtx = path.substring(0, path.lastIndexOf("/"));
		taskServiceBaseUrl = appCtx + "/s/" + projectIdentifier + "/tasks/";
	}

	@Override
	protected void bind() {
		// If this user is anonymous, then hide options and features which are only available to logged-in users.
		if (!AuthenticationHelper.isAnonymous()) {
			setupNewTaskButton();
		}
		initializeResultsTable();
	}

	private void setupNewTaskButton() {
		taskListView.newTask.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ProjectNewTaskPlace.createNewTaskPlace(projectIdentifier).go();
			}
		});
	}

	public void doSearch(QueryState queryState) {
		this.queryState = queryState;
		startNewSearch();

		switch (queryState.getQueryType()) {
		case Criteria:
			doSearchWithCriteria(queryState.getQueryString());
			break;
		case Predefined:
			doSearch(queryState.getPredefinedQuery());
			break;
		case Saved:
			doSearch(queryState.getSavedQuery());
			break;
		case Text:
			doSearch(queryState.getQueryString());
			break;
		default:
			throw new IllegalStateException("Illegal query type: " + queryState.getQueryType());
		}
	}

	private void doSearch(String text) {
		taskListView.exportCsvAnchor.setHref(taskServiceBaseUrl + "export/text/csv/" + text);
		taskListView.exportJsonAnchor.setHref(taskServiceBaseUrl + "export/text/json/" + text);
		taskListView.updateLabel(text);
	}

	private void doSearch(PredefinedTaskQuery query) {
		taskListView.exportCsvAnchor.setHref(taskServiceBaseUrl + "export/predefined/csv/" + query.name());
		taskListView.exportJsonAnchor.setHref(taskServiceBaseUrl + "export/predefined/json/" + query.name());
		taskListView.clearSearchInfo();
	}

	private void doSearch(SavedTaskQuery query) {
		doSearchWithCriteria(query.getQueryString());
		taskListView.updateLabel(query);
	}

	private void doSearchWithCriteria(String criteria) {
		taskListView.exportCsvAnchor.setHref(taskServiceBaseUrl + "export/criteria/csv/" + criteria);
		taskListView.exportJsonAnchor.setHref(taskServiceBaseUrl + "export/criteria/json/" + criteria);
		taskListView.updateLabel(criteria);
	}

	// REVIEW : this should be in the view.
	private void initializeResultsTable() {
		if (resultsTable != null) {
			return;
		}
		resultsTable = new CellTable<Task>(15, CellTableResources.get.resources);
		resultsTable.setSelectionModel(new NoSelectionModel<Task>());
		resultsTable.setPageSize(ProjectTasksPlace.DEFAULT_PAGESIZE);
		resultsTable.setStyleName("tasks");
		resultsTable.setRowStyles(new RowStyles<Task>() {
			public String getStyleNames(Task row, int rowIndex) {
				if (row.getStatus().getValue().equals("NEW")) {
					return "new";
				}
				return null;
			}
		});

		taskListView.pager.setDisplay(resultsTable);

		taskListColumnConfiguration = new TaskListColumnConfiguration(this, resultsTable, selectionModel);
		taskListColumnConfiguration.configureFromRequest();
		taskListColumnConfiguration.apply();
		taskListView.tasksPanel.add(resultsTable);

	}

	private void startNewSearch() {
		initializeViewAdapter();

		int indexStart = queryState.getQueryRequest().getPageInfo().getOffset();
		int resultSize = queryState.getQueryRequest().getPageInfo().getSize();
		resultsTable.getColumnSortList().clear();
		if (queryState.getQueryRequest().getSortInfo() != null) {
			Column<?, ?> csi = getColumnSortInfo(queryState.getQueryRequest().getSortInfo());
			if (csi != null) {
				resultsTable.getColumnSortList().push(csi);
			} else {
				ProfileGinjector.get.instance().getNotifier()
						.displayMessage(new Message(10, "Unknown sort", MessageType.ERROR));
			}
		}
		// This will trigger a call to fetchRange()
		resultsTable.setVisibleRangeAndClearData(new Range(indexStart, resultSize), true);
	}

	private void initializeViewAdapter() {
		if (dataProvider == null) {
			// use a local variable so that initialization doesn't cause a refresh.
			AsyncDataProvider<Task> dataProvider = new AsyncDataProvider<Task>() {
				@Override
				protected void onRangeChanged(HasData<Task> view) {
					if (TaskListPresenter.this.dataProvider == null) {
						return;
					}
					fetchRange(view.getVisibleRange());
				}
			};
			dataProvider.addDataDisplay(resultsTable);
			AsyncHandler columnSortHandler = new AsyncHandler(resultsTable);
			resultsTable.addColumnSortHandler(columnSortHandler);

			this.dataProvider = dataProvider;
		}
	}

	protected void fetchRange(final Range range) {
		QueryRequest request = new QueryRequest();
		request.setPageInfo(new Region(range.getStart(), range.getLength()));
		SortInfo sortInfo = getSortInfoFromTable();
		if (sortInfo != null && sortInfo.getSortField() != null && sortInfo.getSortOrder() != null) {
			request.setSortInfo(sortInfo);
		}

		queryState.setQueryRequest(request);
		// Update the current URL to represent the new page and sort parameters
		updateCurrentUrlForQuery(request);

		final AsyncCallbackSupport<TaskQueryResult> callback = new AsyncCallbackSupport<TaskQueryResult>() {

			@Override
			protected void success(TaskQueryResult actionResult) {
				QueryResult<Task> result = actionResult.get();
				// TODO: when range starts at 0 and no results found, hide
				// results table and show 'no results found' message
				dataProvider.updateRowCount(result.getTotalResultSize(), true);
				// set the length to less than one page when necessary
				dataProvider.updateRowData(range.getStart(), result.getResultPage());
			}

			@Override
			public void onFailure(Throwable exception) {
				super.onFailure(exception);
				dataProvider.updateRowCount(0, false);
			}
		};

		TaskQueryAction action = new TaskQueryAction(getProjectIdentifier(), queryState);
		getDispatchService().execute(action, callback);
	}

	private SortInfo getSortInfoFromTable() {
		if (resultsTable.getColumnSortList().size() == 0) {
			return null;
		}
		ColumnSortInfo sort = resultsTable.getColumnSortList().get(0);
		String fieldId = taskListColumnConfiguration.getDescriptor(sort.getColumn()).getTaskField();
		return new SortInfo(fieldId, sort.isAscending() ? Order.ASCENDING : Order.DESCENDING);
	}

	private Column<?, ?> getColumnSortInfo(SortInfo sortInfo) {
		TaskColumnDescriptor descriptor = taskListColumnConfiguration.getDescriptorById(sortInfo.getSortField());
		if (descriptor == null) {
			return null;
		}
		return descriptor.getColumn();
	}

	private void updateCurrentUrlForQuery(QueryRequest request) {
		// FIXME / TODO use the logic in ProjectTasksPlace.
		// Get our current page URL

		SortInfo sortInfo = request.getSortInfo();
		// Get our current parameters.
		Map<String, List<String>> urlParamMap = Navigation.getNavigationParameterMap();

		// Append our parameters as appropriate
		if (sortInfo != null) {
			updateParamInMap(urlParamMap, ProjectTasksPlace.SORTFIELD_URLPARAM, sortInfo.getSortField(),
					sortInfo.getSortField() != null);
			updateParamInMap(urlParamMap, ProjectTasksPlace.SORTORDER_URLPARAM,
					String.valueOf(sortInfo.getSortOrder()), sortInfo.getSortOrder() != null);
		}

		Region requestRegion = request.getPageInfo();
		updateParamInMap(urlParamMap, ProjectTasksPlace.NUMRESULTS_URLPARAM, String.valueOf(requestRegion.getSize()),
				requestRegion.getSize() != ProjectTasksPlace.DEFAULT_PAGESIZE);
		updateParamInMap(urlParamMap, ProjectTasksPlace.STARTINDEX_URLPARAM, String.valueOf(requestRegion.getOffset()),
				requestRegion.getOffset() > 0);

		String updatedUrl = History.getToken();

		// Strip off the set of URL parameters on the current history token if they exist.
		if (updatedUrl.indexOf('?') > 0) {
			updatedUrl = updatedUrl.substring(0, updatedUrl.indexOf('?'));
		}

		// Now, loop through and write all of our parameters to our URL.
		for (String curParamName : urlParamMap.keySet()) {
			for (String curParamValue : urlParamMap.get(curParamName)) {
				updatedUrl = Navigation.appendQueryParameterToUrl(updatedUrl, curParamName, curParamValue);
			}
		}

		// Update our URL, but don't trigger a page navigation - it's already in progress.
		History.newItem(updatedUrl, false);
	}

	private void updateParamInMap(Map<String, List<String>> paramMap, String paramName, String paramValue,
			boolean conditionMet) {
		// First, clear any existing value
		paramMap.remove(paramName);

		// Then, plug in our value if appropriate
		if (conditionMet) {
			paramMap.put(paramName, Collections.singletonList(paramValue));
		}
	}

	/**
	 * 
	 */
	public void onQuerySaved() {
		taskListView.saveSuccess();
	}
}
