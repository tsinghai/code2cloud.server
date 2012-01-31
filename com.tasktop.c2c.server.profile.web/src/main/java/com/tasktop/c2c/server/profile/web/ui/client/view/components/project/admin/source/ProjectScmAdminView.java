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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

public class ProjectScmAdminView extends AbstractComposite implements
		IProjectAdminSourceView<IProjectAdminSourceView.Presenter> {

	interface Binder extends UiBinder<Widget, ProjectScmAdminView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private static ProjectScmAdminView instance;

	public static ProjectScmAdminView getInstance() {
		if (instance == null) {
			instance = new ProjectScmAdminView();
		}
		return instance;
	}

	// Internal
	@UiField
	Panel internalRepositoriesPanel;
	@UiField
	Anchor addInternalRepositoryLink;
	NewHostedRepoDialog newHostedRepoDialog;

	// External
	@UiField
	Panel externalRepositoriesPanel;
	@UiField
	Anchor addExternalRepositoryLink;
	NewExternalRepoDialog newExternalRepoDialog;

	private Presenter presenter;

	private ProjectScmAdminView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("addInternalRepositoryLink")
	void onAddInternalRepository(ClickEvent event) {
		newHostedRepoDialog.center();
		newHostedRepoDialog.show();
	}

	@UiHandler("addExternalRepositoryLink")
	void onAddExternalRepository(ClickEvent event) {
		newExternalRepoDialog.center();
		newExternalRepoDialog.show();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		newHostedRepoDialog = NewHostedRepoDialog.getInstance(presenter);
		newHostedRepoDialog.hide();
		newExternalRepoDialog = NewExternalRepoDialog.getInstance(presenter);
		newExternalRepoDialog.hide();

		setRepositories(presenter.getRepositories());
		setRepoBaseUrl(presenter.getRepoBaseUrl());
	}

	private HandlerRegistration lastDeleteClickHandler = null;

	/**
	 * @param value
	 * @param repoView
	 */
	private void doRemove(final ScmRepository value, final ScmItemView repoView) {
		if (lastDeleteClickHandler != null) {
			lastDeleteClickHandler.removeHandler();
		}
		ConfirmDeleteRepoDialog.getInstance(presenter, value).center();
	}

	private void setRepositories(List<ScmRepository> repositories) {
		if (repositories == null) {
			return;
		}
		externalRepositoriesPanel.clear();
		internalRepositoriesPanel.clear();

		for (final ScmRepository repo : repositories) {
			final ScmItemView repoView = new ScmItemView(repo);
			repoView.removeAnchor.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					doRemove(repo, repoView);
				}
			});

			if (ScmLocation.EXTERNAL.equals(repo.getScmLocation())) {
				externalRepositoriesPanel.add(repoView);
			} else {
				internalRepositoriesPanel.add(repoView);
			}
		}
	}

	private void setRepoBaseUrl(String repoBaseUrl) {
		newHostedRepoDialog.setBaseUrl(repoBaseUrl);
	}
}
