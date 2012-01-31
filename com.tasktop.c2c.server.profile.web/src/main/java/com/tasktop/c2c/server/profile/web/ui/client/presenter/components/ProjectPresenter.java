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

import net.customware.gwt.dispatch.shared.DispatchException;


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectView;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageResult;

public class ProjectPresenter extends AbstractProfilePresenter implements SplittableActivity {

	private Project project;
	private ProjectView view;

	public ProjectPresenter(ProjectView view) {
		super(view);
		this.view = view;
	}

	public ProjectPresenter() {
		this(ProjectView.getInstance());
	}

	public void setPlace(Place p) {
		ProjectHomePlace place = (ProjectHomePlace) p;

		this.project = place.getProject();
		view.setProjectData(place.getProject(), place.getRepositories());
		view.activityView.renderActivity(place.getProjectActivity());

		if (isWikiServiceAvailable(project)) {
			view.setHasWikiService(true);
			AppGinjector.get
					.instance()
					.getDispatchService()
					.execute(new RetrievePageAction(project.getIdentifier(), ProjectHomePlace.WIKI_HOME_PAGE, true),
							new AsyncCallbackSupport<RetrievePageResult>() {
								@Override
								public void success(RetrievePageResult actionResult) {
									view.setProjectWikiPage(actionResult.get());
								}

								@Override
								public void onFailure(Throwable exception) {
									// NoSuchEntity is expected if the homepage doesn't exist - if it was anything else,
									// go to the generic error handler
									if (exception instanceof DispatchException
											&& ((DispatchException) exception).getCauseClassname().equals(
													NoSuchEntityException.class.getName())) {
										view.setProjectWikiPage(null);
									} else {
										super.onFailure(exception);
									}
								}
							});
		} else {
			view.setHasWikiService(false);
		}
		view.setHasMavenService(!project.getProjectServicesOfType(ServiceType.MAVEN).isEmpty());
		view.setHasScmService(!project.getProjectServicesOfType(ServiceType.SCM).isEmpty());
	}

	private boolean isWikiServiceAvailable(Project p) {
		for (ProjectService wikiService : p.getProjectServicesOfType(ServiceType.WIKI)) {
			if (wikiService.isAvailable()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void bind() {

	}
}
