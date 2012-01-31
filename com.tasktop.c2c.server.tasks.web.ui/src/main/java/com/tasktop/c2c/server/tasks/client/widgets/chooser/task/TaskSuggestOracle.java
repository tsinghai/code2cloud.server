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
package com.tasktop.c2c.server.tasks.client.widgets.chooser.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.shared.QueryState;
import com.tasktop.c2c.server.tasks.shared.QueryState.QueryType;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.TaskQueryResult;

public class TaskSuggestOracle extends MultiWordSuggestOracle {
	private String projectIdentifier;

	public TaskSuggestOracle(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	public TaskSuggestOracle() {
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public void requestDefaultSuggestions(final Request request, final Callback callback) {
		QueryRequest taskQueryRequest = new QueryRequest();
		taskQueryRequest.setPageInfo(new Region(0, request.getLimit()));

		AsyncCallback<TaskQueryResult> taskQueryCallback = new AsyncCallback<TaskQueryResult>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onSuggestionsReady(request, new Response(new ArrayList<Suggestion>()));
			}

			@Override
			public void onSuccess(TaskQueryResult actionResult) {
				QueryResult<Task> result = actionResult.get();
				callback.onSuggestionsReady(request, new Response(toSuggestions(result.getResultPage())));
			}
		};

		QueryState queryState = new QueryState(PredefinedTaskQuery.RECENT);
		queryState.setQueryRequest(taskQueryRequest);
		CommonGinjector.get.instance().getDispatchService()
				.execute(new TaskQueryAction(projectIdentifier, queryState), taskQueryCallback);
	}

	@Override
	public void requestSuggestions(final Request request, final Callback callback) {
		QueryRequest taskQueryRequest = new QueryRequest();
		taskQueryRequest.setPageInfo(new Region(0, request.getLimit()));
		AsyncCallback<TaskQueryResult> taskQueryCallback = new AsyncCallback<TaskQueryResult>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onSuggestionsReady(request, new Response(new ArrayList<Suggestion>()));
			}

			@Override
			public void onSuccess(TaskQueryResult actionResult) {
				QueryResult<Task> result = actionResult.get();
				callback.onSuggestionsReady(request, new Response(toSuggestions(result.getResultPage())));
			}
		};

		QueryState queryState = new QueryState(QueryType.Text, request.getQuery());
		queryState.setQueryRequest(taskQueryRequest);
		CommonGinjector.get.instance().getDispatchService()
				.execute(new TaskQueryAction(projectIdentifier, queryState), taskQueryCallback);
	}

	protected Collection<? extends Suggestion> toSuggestions(List<Task> tasks) {
		List<Suggestion> suggestions = new ArrayList<Suggestion>(tasks.size());
		for (Task task : tasks) {
			suggestions.add(toSuggestion(task));
		}
		return suggestions;
	}

	protected Suggestion toSuggestion(Task task) {
		return new TaskSuggestion(task);
	}

}
