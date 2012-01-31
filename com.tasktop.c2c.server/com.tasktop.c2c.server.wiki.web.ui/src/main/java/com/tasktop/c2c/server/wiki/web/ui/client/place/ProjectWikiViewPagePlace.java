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
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.WikiPageMappings;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageResult;

public class ProjectWikiViewPagePlace extends AbstractProjectWikiPlace implements HasProjectPlace, HeadingPlace,
		SectionPlace, BreadcrumbPlace {

	public static final String PAGE = "pageName";

	public static class Tokenizer implements PlaceTokenizer<ProjectWikiViewPagePlace> {

		@Override
		public ProjectWikiViewPagePlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projId = pathArgs.getString(Path.PROJECT_ID);
			String pageName = pathArgs.getString(ProjectWikiViewPagePlace.PAGE);
			String anchor = new Path(token).getHashTag();

			if (StringUtils.hasText(anchor)) {
				return createPlaceForPageAnchor(projId, pageName, anchor);
			} else {
				return createPlaceForPage(projId, pageName);
			}
		}

		@Override
		public String getToken(ProjectWikiViewPagePlace place) {
			return place.getToken();
		}
	}

	private List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
	private String pagePath;
	private String anchor;
	private Page page;
	private Boolean enableEdit;
	private Boolean enableDelete;

	private ProjectWikiViewPagePlace(String projectId, String pagePath, String anchor) {
		super(projectId);
		this.pagePath = pagePath;
		this.anchor = anchor;
	}

	public static ProjectWikiViewPagePlace createPlaceForPage(String projectIdentifier, String pagePath) {
		return new ProjectWikiViewPagePlace(projectIdentifier, pagePath, null);
	}

	public static ProjectWikiViewPagePlace createPlaceForPageAnchor(String projectIdentifier, String pagePath,
			String anchor) {
		return new ProjectWikiViewPagePlace(projectIdentifier, pagePath, anchor);
	}

	public static ProjectWikiViewPagePlace createPlaceWithData(Project project, Page page) {
		ProjectWikiViewPagePlace place = new ProjectWikiViewPagePlace(project.getIdentifier(), page.getPath(), null);
		place.setPage(page);
		place.project = project;
		return place;
	}

	public String getHeading() {
		return project.getName();
	}

	public Page getPage() {
		return page;
	}

	public String getAnchor() {
		return anchor;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	@Override
	public Section getSection() {
		return Section.WIKI;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new RetrievePageAction(projectId, pagePath, true));
	}

	protected boolean handleExceptionInResults() {
		// Assume its a page not found
		ProfileGinjector.get.instance().getNotifier()
				.displayMessage(Message.createErrorMessage("Page \"" + pagePath + "\" not found."));
		return true;
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		RetrievePageResult pageResult = getResult(RetrievePageResult.class);
		if (pageResult != null) {
			page = pageResult.get();
		}
		if (this.enableDelete == null) {
			this.enableDelete = !AuthenticationHelper.isAnonymous();
		}
		if (this.enableEdit == null) {
			this.enableEdit = !AuthenticationHelper.isAnonymous();
		}
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectWikiHomePlace.createDefaultPlace(projectId).getHistoryToken(), "Wiki"));
		breadcrumbs.add(new Breadcrumb(getHistoryToken(), pagePath));
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		String fullPagePath = pagePath;

		if (StringUtils.hasText(anchor)) {
			fullPagePath += Path.HASHTAG_DELIMITER + anchor;
		}

		tokenMap.put(ProjectWikiViewPagePlace.PAGE, fullPagePath);

		return WikiPageMappings.ProjectWikiViewPage.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public String getWindowTitle() {
		return "Wiki - " + pagePath + " - " + project.getName() + " - " + WindowTitleBuilder.PRODUCT_NAME;
	}

	/**
	 * @return the enableEdit
	 */
	public boolean isEnableEdit() {
		return enableEdit;
	}

	/**
	 * @param enableEdit
	 *            the enableEdit to set
	 */
	public void setEnableEdit(boolean enableEdit) {
		this.enableEdit = enableEdit;
	}

	/**
	 * @return the enableDelete
	 */
	public boolean isEnableDelete() {
		return enableDelete;
	}

	/**
	 * @param enableDelete
	 *            the enableDelete to set
	 */
	public void setEnableDelete(boolean enableDelete) {
		this.enableDelete = enableDelete;
	}

	/**
	 * @return the pagePath
	 */
	public String getPagePath() {
		return pagePath;
	}
}
