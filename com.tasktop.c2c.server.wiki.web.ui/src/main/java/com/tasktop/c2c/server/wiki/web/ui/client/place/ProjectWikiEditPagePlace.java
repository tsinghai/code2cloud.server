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
package com.tasktop.c2c.server.wiki.web.ui.client.place;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.WikiPageMappings;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsResult;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageResult;

public class ProjectWikiEditPagePlace extends AbstractProjectWikiPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace {

	public static class Tokenizer implements PlaceTokenizer<ProjectWikiEditPagePlace> {

		@Override
		public ProjectWikiEditPagePlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projId = pathArgs.getString(Path.PROJECT_ID);
			String pageName = pathArgs.getString(ProjectWikiViewPagePlace.PAGE);
			if (StringUtils.hasText(pageName)) {
				return createPlaceForPath(projId, pageName);
			} else {
				return createPlaceForNewPage(projId);
			}
		}

		@Override
		public String getToken(ProjectWikiEditPagePlace place) {
			return place.getProjectId();
		}
	}

	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	private boolean isNew;
	private String pagePath;
	private Page page;
	private List<Attachment> attachements;

	public static ProjectWikiEditPagePlace createPlaceForPath(String projectIdentifier, String path) {
		return new ProjectWikiEditPagePlace(projectIdentifier, path);
	}

	public static ProjectWikiEditPagePlace createPlaceForNewPage(String projectIdentifier) {
		return new ProjectWikiEditPagePlace(projectIdentifier, null);
	}

	private ProjectWikiEditPagePlace(String projectIdentifier, String pagePath) {
		super(projectIdentifier);
		this.pagePath = pagePath;
	}

	public String getHeading() {
		return project.getName();
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		if (StringUtils.hasText(pagePath)) {
			tokenMap.put(ProjectWikiViewPagePlace.PAGE, pagePath);
		}

		return WikiPageMappings.ProjectWikiEditPage.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new RetrievePageAction(projectId, pagePath, false));
		actions.add(new ListAttachmentsAction(projectId, pagePath));
	}

	protected boolean handleExceptionInResults() {
		// Assume its a page not found
		return true;
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		RetrievePageResult retrievePageResult = getResult(RetrievePageResult.class);
		isNew = retrievePageResult == null;
		if (!isNew) {
			page = retrievePageResult.get();
			attachements = getResult(ListAttachmentsResult.class).get();
		}
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectWikiHomePlace.createDefaultPlace(projectId).getHistoryToken(), "Wiki"));

		if (!isNew) {
			breadcrumbs.add(new Breadcrumb(ProjectWikiViewPagePlace.createPlaceForPage(projectId, pagePath)
					.getHistoryToken(), pagePath));
			breadcrumbs.add(new Breadcrumb(getHistoryToken(), "Edit"));
		} else {
			breadcrumbs.add(new Breadcrumb(getHistoryToken(), "New Page"));

		}
	}

	public String getPath() {
		return pagePath;
	}

	/**
	 * @return the page
	 */
	public Page getPage() {
		return page;
	}

	@Override
	public String getWindowTitle() {
		if (pagePath != null) {
			return "Edit Wiki - " + pagePath + " - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
		} else {
			return "New Wiki - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
		}
	}

	/**
	 * @return
	 */
	public List<Attachment> getAttachements() {
		return attachements;
	}

	public boolean isNew() {
		return isNew;
	}

}
