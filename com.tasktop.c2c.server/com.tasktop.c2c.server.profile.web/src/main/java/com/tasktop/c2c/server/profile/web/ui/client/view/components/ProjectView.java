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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiPageContentPresenter;
import com.tasktop.c2c.server.wiki.web.ui.client.view.WikiPageContentView;

public class ProjectView extends AbstractComposite {

	private static ProjectView instance = null;

	public static ProjectView getInstance() {
		if (instance == null) {
			instance = new ProjectView();
		}
		return instance;
	}

	interface Binder extends UiBinder<Widget, ProjectView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private Project project = null;

	@UiField
	Label description;

	@UiField
	Panel scmPanel;
	@UiField
	TextBox mavenUrlTextBox;
	@UiField
	TextBox mavenDavUrlTextBox;

	@UiField
	Anchor readMoreAnchor;

	@UiField
	HTMLPanel wikiWrapper;
	@UiField
	Anchor wikiHomePageLink;
	@UiField
	public HasOneWidget wikiContentPanel;
	@UiField
	public SimpleActivityView activityView;
	@UiField
	DivElement scmSectionDiv;
	@UiField
	DivElement mavenSectionDiv;

	private List<ScmRepository> repositories;

	private ProjectView() {
		initWidget(uiBinder.createAndBindUi(this));
		setupSelectOnClick(mavenUrlTextBox);
		setupSelectOnClick(mavenDavUrlTextBox);
		readMoreAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (wikiContentPanel.getWidget() != null) {
					wikiContentPanel.getWidget().getElement().scrollIntoView();
				}
			}
		});
	}

	public void setProjectData(Project project, List<ScmRepository> repositoryNames) {
		this.project = project;
		this.repositories = repositoryNames;
		updateUi();
	}

	/**
	 * Set the wiki page for the project. Null page implies the page has not been created.
	 * 
	 * @param page
	 */
	public void setProjectWikiPage(Page page) {

		setHasWikiHome(page != null);

		if (page != null) {
			WikiPageContentPresenter wikiPagePresenter = new WikiPageContentPresenter(new WikiPageContentView());
			ProjectWikiViewPagePlace place = ProjectWikiViewPagePlace.createPlaceWithData(project, page);
			place.setEnableEdit(false);
			place.setEnableDelete(false);
			wikiPagePresenter.setPlace(place);
			wikiPagePresenter.setEnableMetadata(false);
			wikiPagePresenter.setEnableHeader(false);
			wikiPagePresenter.go(wikiContentPanel);
		}
	}

	private void updateUi() {

		description.setText(project.getDescription());
		scmPanel.clear();

		for (ScmRepository repository : repositories) {
			scmPanel.add(new ProjectScmRepositoryRow(repository));

		}

		ProjectService mavenService = null;
		if (project.getProjectServices() != null) {
			for (ProjectService service : project.getProjectServices()) {
				if (service.getServiceType().equals(ServiceType.MAVEN)) {
					mavenService = service;
					break;
				}
			}
		}

		if (mavenService != null) {
			mavenUrlTextBox.setText(mavenService.getUrl());
			mavenDavUrlTextBox.setText("dav:" + mavenService.getUrl());
		} else {
			mavenUrlTextBox.setText("");
			mavenDavUrlTextBox.setText("");
		}

		wikiHomePageLink.setHref(ProjectWikiViewPagePlace.createPlaceForPage(project.getIdentifier(),
				ProjectHomePlace.WIKI_HOME_PAGE).getHref());

	}

	private void setHasWikiHome(boolean hasWikiHome) {
		wikiHomePageLink.setVisible(hasWikiHome);
		if (!hasWikiHome) {
			wikiContentPanel.setWidget(new Label("You can add content here by creating a wiki page called \"Home\"."));
		}
	}

	public static void setupSelectOnClick(final TextBox box) {
		box.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (box.getSelectionLength() == 0) {
					box.selectAll();
				}
			}
		});
	}

	public void setHasWikiService(boolean hasWiki) {
		readMoreAnchor.setVisible(hasWiki);
		wikiWrapper.setVisible(hasWiki);
		// wikiHomePageLink.setVisible(hasWiki);
	}

	public void setHasMavenService(boolean hasMaven) {
		UIObject.setVisible(mavenSectionDiv, hasMaven);
	}

	public void setHasScmService(boolean hasScm) {
		UIObject.setVisible(scmSectionDiv, hasScm);
	}
}
