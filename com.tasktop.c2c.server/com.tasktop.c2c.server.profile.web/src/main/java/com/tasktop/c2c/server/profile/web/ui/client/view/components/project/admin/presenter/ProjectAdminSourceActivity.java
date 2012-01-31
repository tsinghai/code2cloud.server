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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.presenter;

import java.util.List;


import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectScmRepositoriesResult;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminSourcePlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source.IProjectAdminSourceView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source.ProjectScmAdminView;

public class ProjectAdminSourceActivity extends AbstractActivity implements IProjectAdminSourceView.Presenter,
		SplittableActivity {

	private String projectIdentifier;
	private String repoBaseUrl;
	private List<ScmRepository> repositories;
	private Project project;
	private ProjectScmAdminView view = ProjectScmAdminView.getInstance();

	public ProjectAdminSourceActivity() {
	}

	public void setPlace(Place p) {
		ProjectAdminSourcePlace place = (ProjectAdminSourcePlace) p;
		this.projectIdentifier = place.getProjectIdentifer();
		this.project = place.getProject();
		this.repositories = place.getRepositories();
		this.repoBaseUrl = place.getGitBaseUrl();
		updateView();
	}

	private void updateView() {
		view.setPresenter(this);
	}

	public String getRepoBaseUrl() {
		return repoBaseUrl;
	}

	public List<ScmRepository> getRepositories() {
		return repositories;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public void start(final AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
	}

	@Override
	public void onDeleteRepository(final Long repositoryId) {
		OperationMessage message = new OperationMessage("Deleting repository");
		message.setSuccessText("Repository deleted");
		ProfileEntryPoint.getInstance().getProfileService()
				.deleteProjectGitRepository(projectIdentifier, repositoryId, new AsyncCallbackSupport<Void>(message) {
					@Override
					protected void success(Void result) {
						for (ScmRepository repository : repositories) {
							if (repository.getId().equals(repositoryId)) {
								repositories.remove(repository);
								break;
							}
						}
						updateView();
					}
				});
	}

	@Override
	public void onCreateRepository(ErrorCapableView errorView, ScmRepository repository) {
		OperationMessage message = new OperationMessage("Adding repository");
		message.setSuccessText("Repository added");
		ProfileEntryPoint
				.getInstance()
				.getProfileService()
				.createProjectGitRepository(projectIdentifier, repository,
						new AsyncCallbackSupport<Void>(message, errorView) {
							@Override
							protected void success(final Void result) {
								AppGinjector.get
										.instance()
										.getDispatchService()
										.execute(new GetProjectScmRepositoriesAction(projectIdentifier),
												new AsyncCallbackSupport<GetProjectScmRepositoriesResult>() {

													@Override
													protected void success(GetProjectScmRepositoriesResult result) {
														repositories = result.get();
														updateView();
													}
												});
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Repository added"));
							}

						});
	}
}
