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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;


import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AbstractBatchFetchingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.ui.client.navigation.PageMappings;

/**
 * This is used by hudson to hook into the GWT header and footer.
 * 
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class AppSectionPlace extends AbstractBatchFetchingPlace implements HeadingPlace, HasProjectPlace,
		BreadcrumbPlace, SectionPlace, WindowTitlePlace {

	public enum AppSection {
		HEADER("header-wrapper"), FOOTER("footer-stretch");

		private final String styleName;

		private AppSection(String styleName) {
			this.styleName = styleName;
		}

		public String getStyleName() {
			return styleName;
		}
	}

	public static final String SECTION = "displaySection";

	public static class Tokenizer implements PlaceTokenizer<AppSectionPlace> {

		@Override
		public AppSectionPlace getPlace(String token) {
			// Tokenize our URL now.
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			return createPlace(pathArgs.getString(Path.PROJECT_ID), pathArgs.getString(SECTION));
		}

		@Override
		public String getToken(AppSectionPlace place) {
			return place.getToken();
		}
	}

	private final String projectId;
	private Project project;
	private List<Breadcrumb> breadcrumbs = Collections.EMPTY_LIST;
	private AppSection appSection;

	public static AppSectionPlace createPlace(String projectId, String sectionStyle) {
		return new AppSectionPlace(projectId, sectionStyle);
	}

	private AppSectionPlace(String projectId, String sectionStyle) {
		this.projectId = projectId;
		for (AppSection s : AppSection.values()) {
			if (s.getStyleName().equals(sectionStyle)) {
				this.appSection = s;
			}
		}
		if (AppSection.FOOTER.equals(appSection)) {
			super.requiresUserInfo = false;
		}
	}

	public AppSection getSectionToShow() {
		return this.appSection;
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);
		tokenMap.put(SECTION, appSection.getStyleName());

		return PageMappings.AppSection.getUrlForNamedArgs(tokenMap);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public String getHeading() {
		if (project != null) {
			return project.getName();
		}
		return "";
	}

	@Override
	public Section getSection() {
		return Section.BUILDS;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		if (AppSection.HEADER.equals(appSection)) {
			actions.add(new GetProjectAction(projectId));
		}
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		if (AppSection.HEADER.equals(appSection)) {
			project = getResult(GetProjectResult.class).get();
			createBreadcrumbs(project);
		}
		onPlaceDataFetched();
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
	}

	@Override
	public String getWindowTitle() {
		if (project != null) {
			return WindowTitleBuilder.createWindowTitle(Section.BUILDS, project.getName());
		}
		return null; // Don't use this title.
	}
}
