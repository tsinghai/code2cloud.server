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
package com.tasktop.c2c.server.tasks.client.place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.shared.Action;

import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Navigation;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.tasks.client.TaskPageMappings;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.shared.QueryState;
import com.tasktop.c2c.server.tasks.shared.QueryState.QueryType;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;

/**
 * @author jtyrrell
 * @author cmorgan
 * 
 */
public class ProjectTasksPlace extends AbstractProjectTaskBatchingPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace {

	public static final String NAMED_Q = "namedQuery";
	public static final String TEXT_Q = "textQuery";
	public static final String CRIT_Q = "critQuery";

	public static final String SORTFIELD_URLPARAM = "sort";
	public static final String SORTORDER_URLPARAM = "order";
	public static final String STARTINDEX_URLPARAM = "idx";
	public static final String NUMRESULTS_URLPARAM = "num";
	public static final int DEFAULT_PAGESIZE = 25;

	public static class Tokenizer implements PlaceTokenizer<ProjectTasksPlace> {

		@Override
		public ProjectTasksPlace getPlace(String token) {
			// First, strip off any query params so they don't clutter our URL generation
			String cleanedToken = stripQueryString(token);
			Args pathArgs = PageMapping.getPathArgsForUrl(cleanedToken);

			String projId = pathArgs.getString(Path.PROJECT_ID);

			// Only one of the following three can be defined at a time, so use this to determine which place to create.
			String namedQuery = pathArgs.getString(NAMED_Q);
			String textQuery = pathArgs.getString(TEXT_Q);
			String critQuery = pathArgs.getString(CRIT_Q);

			String queryString = getQueryString(token);

			int pageSize = DEFAULT_PAGESIZE;
			int startIdx = 0;
			SortInfo sortInfo = null;
			if (queryString != null) {
				Map<String, String> queryMap = buildParamMap(queryString);
				String sortField = queryMap.get(SORTFIELD_URLPARAM);
				String sortOrder = queryMap.get(SORTORDER_URLPARAM);
				String startIndex = queryMap.get(STARTINDEX_URLPARAM);
				String numResults = queryMap.get(NUMRESULTS_URLPARAM);
				if (sortField != null) {
					if (sortOrder == null) {
						sortInfo = new SortInfo(sortField);
					} else {
						sortInfo = new SortInfo(sortField, SortInfo.Order.valueOf(sortOrder));
					}
				}
				if (numResults != null) {
					pageSize = Integer.parseInt(numResults);
				}
				if (startIndex != null) {
					startIdx = Integer.parseInt(startIndex);
				}

			}
			QueryRequest queryRequest = new QueryRequest(new Region(startIdx, pageSize), sortInfo);

			ProjectTasksPlace place;
			if (StringUtils.hasText(critQuery)) {
				// We have a criteria query.
				place = createPlaceForCriteriaQuery(projId, critQuery);
			} else if (StringUtils.hasText(textQuery)) {
				// We have a text query.
				place = createPlaceForTextQuery(projId, textQuery);
			} else if (StringUtils.hasText(namedQuery)) {
				// We have a named (predefined or saved) query.
				place = createPlaceForNamedQuery(projId, namedQuery);
			} else {
				place = createDefaultPlace(projId);
			}
			place.getQueryState().setQueryRequest(queryRequest);
			return place;
		}

		@Override
		public String getToken(ProjectTasksPlace place) {
			return place.getToken();
		}

		private String stripQueryString(String queryText) {
			int indexOfQueryString = queryText.indexOf('?');
			if (indexOfQueryString != -1) {
				queryText = queryText.substring(0, indexOfQueryString);
			}
			return queryText;
		}

		private String getQueryString(String queryText) {
			int indexOfQueryString = queryText.indexOf('?');
			if (indexOfQueryString != -1) {
				return queryText.substring(indexOfQueryString + 1);
			}
			return null;
		}

		private static Map<String, String> buildParamMap(String queryString) {
			Map<String, String> result = new HashMap<String, String>();

			if (queryString != null && queryString.length() > 1) {

				for (String kvPair : queryString.split("&")) {
					String[] kv = kvPair.split("=", 2);
					if (kv[0].length() == 0) {
						continue;
					}

					result.put(kv[0], kv.length > 1 ? URL.decodeQueryString(kv[1]) : "");
				}
			}

			return result;
		}

	}

	private QueryState queryState;
	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	private RepositoryConfiguration repositoryConfiguration;
	private static ProjectTasksPlace lastVisited = null;

	private ProjectTasksPlace(String projectId, QueryType type, String queryName) {
		super(projectId);
		this.queryState = new QueryState(type, queryName);
		this.queryState.setQueryRequest(new QueryRequest(new Region(0, DEFAULT_PAGESIZE), null));
	}

	public static ProjectTasksPlace createDefaultPlace(String projectId) {
		return new ProjectTasksPlace(projectId, QueryType.Default, null);
	}

	public static ProjectTasksPlace createPlaceForTextQuery(String projectId, String queryStr) {
		return new ProjectTasksPlace(projectId, QueryType.Text, queryStr);
	}

	public static ProjectTasksPlace createPlaceForCriteriaQuery(String projectId, String queryStr) {
		return new ProjectTasksPlace(projectId, QueryType.Criteria, queryStr);
	}

	public static ProjectTasksPlace createPlaceForPredefinedQuery(String projectId, PredefinedTaskQuery taskType) {
		return createPlaceForNamedQuery(projectId, taskType.name().toLowerCase());
	}

	public static ProjectTasksPlace createPlaceForNamedQuery(String projectId, String queryName) {
		return new ProjectTasksPlace(projectId, QueryType.Named, queryName);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	public Section getSection() {
		return Section.TASKS;
	}

	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		switch (this.queryState.getQueryType()) {
		case Criteria:
			tokenMap.put(CRIT_Q, this.queryState.getQueryString());
			break;
		case Text:
			tokenMap.put(TEXT_Q, this.queryState.getQueryString());
			break;
		case Named:
			tokenMap.put(NAMED_Q, this.queryState.getQueryString());
			break;
		case Predefined:
			tokenMap.put(NAMED_Q, this.queryState.getPredefinedQuery().name().toLowerCase());
			break;
		case Saved:
			tokenMap.put(NAMED_Q, this.queryState.getSavedQuery().getName());
			break;

		}
		String url = TaskPageMappings.ProjectTasks.getUrlForNamedArgs(tokenMap);
		Map<String, String> queryParams = new LinkedHashMap<String, String>();
		if (this.queryState.getQueryRequest() != null) {
			Region r = this.queryState.getQueryRequest().getPageInfo();
			if (r != null) {
				if (r.getOffset() != null && !r.getOffset().equals(0)) {
					queryParams.put(STARTINDEX_URLPARAM, r.getOffset().toString());
				}
				if (r.getSize() != null && !r.getSize().equals(DEFAULT_PAGESIZE)) {
					queryParams.put(NUMRESULTS_URLPARAM, r.getSize().toString());
				}
			}
			SortInfo s = this.queryState.getQueryRequest().getSortInfo();
			if (s != null) {
				if (s.getSortField() != null) {
					queryParams.put(SORTFIELD_URLPARAM, s.getSortField());
				}
				if (s.getSortOrder() != null) {
					queryParams.put(SORTORDER_URLPARAM, s.getSortOrder().name());
				}
			}
		}
		if (!queryParams.isEmpty()) {
			for (Entry<String, String> e : queryParams.entrySet()) {
				url = Navigation.appendQueryParameterToUrl(url, e.getKey(), e.getValue());
			}
		}
		return url;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetRepositoryConfigurationAction(projectId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		repositoryConfiguration = getResult(GetRepositoryConfigurationResult.class).get();

		if (queryState.getQueryType().equals(QueryType.Default)) {
			setupDefaultQuery();
		} else if (queryState.getQueryType().equals(QueryType.Named)) {
			if (!resolveNamedQuery(queryState.getQueryString())) {
				ProfileGinjector.get.instance().getNotifier()
						.displayMessage(Message.createErrorMessage("No query named: " + queryState.getQueryString()));
				return;
			}
		}
		lastVisited = this;
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	/** @return true if resolved */
	private boolean resolveNamedQuery(String queryName) {

		for (PredefinedTaskQuery predefinedQuery : PredefinedTaskQuery.values()) {
			if (predefinedQuery.toString().equalsIgnoreCase((queryName))) {
				this.queryState.setQueryType(QueryType.Predefined);
				this.queryState.setPredefinedQuery(predefinedQuery);
				return true;
			}
		}
		for (SavedTaskQuery query : repositoryConfiguration.getSavedTaskQueries()) {
			if (query.getName().equalsIgnoreCase((queryName))) {
				this.queryState.setQueryType(QueryType.Saved);
				this.queryState.setSavedQuery(query);
				return true;
			}
		}
		return false;
	}

	private void setupDefaultQuery() {
		if (lastVisited != null && lastVisited.projectId.equals(this.projectId)) {
			queryState = lastVisited.queryState;
			return;
		}
		queryState.setQueryType(QueryType.Predefined);
		queryState.setPredefinedQuery(PredefinedTaskQuery.DEFAULT);
		queryState.setQueryRequest(new QueryRequest());
		queryState.getQueryRequest().setPageInfo(new Region(0, ProjectTasksPlace.DEFAULT_PAGESIZE));
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Tasks"));
	}

	/**
	 * @return the queryState
	 */
	public QueryState getQueryState() {
		return queryState;
	}

	/**
	 * @param queryState
	 *            the queryState to set
	 */
	public void setQueryState(QueryState queryState) {
		this.queryState = queryState;
	}

	/**
	 * @return the repositoryConfiguration
	 */
	public RepositoryConfiguration getRepositoryConfiguration() {
		return repositoryConfiguration;
	}

	@Override
	public String getWindowTitle() {
		String querySummary;
		switch (queryState.getQueryType()) {
		case Criteria:
			querySummary = "Advanced Search - ";
			break;
		case Predefined:
			querySummary = queryState.getPredefinedQuery().getLabel() + " - ";
			break;
		case Saved:
			querySummary = queryState.getSavedQuery().getName() + " - ";
			break;
		case Text:
			querySummary = "Search for '" + queryState.getQueryString() + "' - ";
			break;
		case Default:// Should never happen
		case Named:
		default:
			querySummary = "";

		}
		return querySummary + "Tasks - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
	}

}
