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

import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.BreadcrumbPlace;
import com.tasktop.c2c.server.profile.web.client.place.HasProjectPlace;
import com.tasktop.c2c.server.profile.web.client.place.HeadingPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SectionPlace;
import com.tasktop.c2c.server.profile.web.client.place.WindowTitlePlace;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEvent;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEventHandler;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEvent;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEventHandler;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.HeaderView;

public class HeaderPresenter extends AbstractProfilePresenter {

	private final HeaderView view;

	public HeaderPresenter(final HeaderView view) {
		super(view);
		this.view = view;

		view.searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doSearch(view.search.getText());
			}
		});
		addAuthListeners();
	}

	private void addAuthListeners() {
		final AppGinjector injector = AppGinjector.get.instance();

		EventBus eventBus = injector.getEventBus();
		eventBus.addHandler(LogoutEvent.TYPE, new LogoutEventHandler() {
			@Override
			public void onLogout() {
				view.setAuthenticated(false);
			}
		});
		eventBus.addHandler(LogonEvent.TYPE, new LogonEventHandler() {
			@Override
			public void onLogon(Credentials credentials) {
				injector.getAppState().setCredentials(credentials);
				view.setAuthenticated(true);
			}
		});
	}

	public void setPlace(Place place) {
		String heading = "";
		List<Breadcrumb> breadcrumbs = Collections.EMPTY_LIST;
		Project project = null;
		Section section = null;
		String windowTitle = WindowTitleBuilder.PRODUCT_NAME;

		if (place instanceof HasProjectPlace) {
			project = ((HasProjectPlace) place).getProject();
		}
		if (place instanceof HeadingPlace) {
			heading = ((HeadingPlace) place).getHeading();
		}
		if (place instanceof BreadcrumbPlace) {
			breadcrumbs = ((BreadcrumbPlace) place).getBreadcrumbs();
		}
		if (place instanceof SectionPlace) {
			section = ((SectionPlace) place).getSection();
		}
		if (place instanceof WindowTitlePlace) {
			windowTitle = ((WindowTitlePlace) place).getWindowTitle();
		}
		String currentQuery = null;
		if (place instanceof ProjectsDiscoverPlace) {
			currentQuery = ((ProjectsDiscoverPlace) place).getQuery();
		}
		view.search.setText(currentQuery);

		view.setProject(project);
		view.setPageTitle(heading);
		view.setBreadcrumbs(breadcrumbs);
		view.setSection(section);
		if (windowTitle != null) {
			Window.setTitle(windowTitle);
		}

		if (getAppState().isUserAnonymous()) {
			view.setAuthenticated(false);
		} else {
			view.setAuthenticated(true);
		}
		view.setGravatarHash(getAppState().getCredentials() == null ? null : getAppState().getCredentials()
				.getProfile().getGravatarHash());

	}

	@Override
	public void bind() {

	}

	public void doSearch(String queryText) {
		if (queryText == null || queryText.trim().length() == 0) {
			return;
		} else {
			ProjectsDiscoverPlace.createPlaceForQuery(queryText).go();
		}
	}
}
