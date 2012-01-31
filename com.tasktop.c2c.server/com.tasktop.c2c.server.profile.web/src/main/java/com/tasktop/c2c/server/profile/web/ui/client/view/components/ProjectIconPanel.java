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


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.event.ClearCacheEvent;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDashboardPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDeploymentPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectTeamPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;

public class ProjectIconPanel extends Composite {

	interface ProjectIconPanelUiBinder extends UiBinder<Widget, ProjectIconPanel> {
	}

	private static ProjectIconPanelUiBinder uiBinder = GWT.create(ProjectIconPanelUiBinder.class);

	@UiField
	Anchor projectHome;
	@UiField
	Anchor dashboard;
	@UiField
	Anchor tasks;
	@UiField
	Anchor builds;
	@UiField
	Anchor deployments;
	@UiField
	Anchor team;
	@UiField
	Anchor wiki;
	@UiField
	Anchor options;
	@UiField
	DivElement optionsWrapper;

	private ProjectOptionsPopupPanel popupPanel = null;
	private Project project;

	public ProjectIconPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		popupPanel = new ProjectOptionsPopupPanel();
		popupPanel.addAutoHidePartner(options.getElement());
		popupPanel.setStyleName("");
	}

	public void setAddStyles(String extraStyleNames) {
		// wrapperList.addClassName(extraStyleNames);
	}

	private static final String ACTIVE_STYLE = "active";
	private static final String DISABLED_STYLE = "disabled working";

	public void activateAllIcons() {
		activateIfNotDisabled(projectHome);
		activateIfNotDisabled(dashboard);
		activateIfNotDisabled(tasks);
		activateIfNotDisabled(builds);
		activateIfNotDisabled(deployments);
		activateIfNotDisabled(team);
		activateIfNotDisabled(wiki);
	}

	private void activateIfNotDisabled(Anchor anchor) {
		if (!anchor.getStyleName().contains(DISABLED_STYLE)) {
			anchor.addStyleName(ACTIVE_STYLE);
		}
	}

	public void setActiveIcon(Section activeIcon) {
		// first, deactivate all of our existing icons.
		projectHome.removeStyleName(ACTIVE_STYLE);
		dashboard.removeStyleName(ACTIVE_STYLE);
		tasks.removeStyleName(ACTIVE_STYLE);
		builds.removeStyleName(ACTIVE_STYLE);
		deployments.removeStyleName(ACTIVE_STYLE);
		team.removeStyleName(ACTIVE_STYLE);
		wiki.removeStyleName(ACTIVE_STYLE);

		// No active icon? Bail out now that all are deactivated.
		if (activeIcon == null) {
			return;
		}

		// Then, activate the appropriate icon
		switch (activeIcon) {
		case BUILDS:
			builds.addStyleName(ACTIVE_STYLE);
			break;
		case DASHBOARD:
			dashboard.addStyleName(ACTIVE_STYLE);
			break;
		case DEPLOYMENTS:
			deployments.addStyleName(ACTIVE_STYLE);
			break;
		case HOME:
			projectHome.addStyleName(ACTIVE_STYLE);
			break;
		case TASKS:
			tasks.addStyleName(ACTIVE_STYLE);
			break;
		case TEAM:
			team.addStyleName(ACTIVE_STYLE);
			break;
		case WIKI:
			wiki.addStyleName(ACTIVE_STYLE);
			break;
		}
	}

	public void setOptionsVisible(boolean visible) {
		UIObject.setVisible(optionsWrapper, visible);
	}

	public void setProject(Project project) {
		this.project = project;
		if (project == null) {
			this.setVisible(false);
			return;
		}
		this.setVisible(true);

		setOptionsVisible(!AuthenticationHelper.isAnonymous());

		projectHome.setHref(ProjectHomePlace.createPlace(project.getIdentifier()).getHref());
		team.setHref(ProjectTeamPlace.createPlace(project.getIdentifier()).getHref());

		boolean allServicesReady = true;

		hideAllServiceLinks();
		if (project.getProjectServices() != null) {
			for (ProjectService projectService : project.getProjectServices()) {

				boolean isAvailable = projectService.isAvailable();

				if (!isAvailable) {
					allServicesReady = false;
				}

				switch (projectService.getServiceType()) {
				case BUILD:
					setServiceLinkEnabled(isAvailable, builds, projectService.getUrl());
					break;

				case TASKS:
					setServiceLinkEnabled(isAvailable, tasks,
							ProjectTasksPlace.createDefaultPlace(project.getIdentifier()).getHref());
					break;
				case WIKI:
					setServiceLinkEnabled(isAvailable, wiki,
							ProjectWikiHomePlace.createDefaultPlace(project.getIdentifier()).getHref());
					break;
				case DEPLOYMENT:
					setServiceLinkEnabled(isAvailable, deployments,
							ProjectDeploymentPlace.createPlace(project.getIdentifier()).getHref());
					break;
				default:
					break;
				}
			}
		}

		setServiceLinkEnabled(true, dashboard, ProjectDashboardPlace.createPlace(project.getIdentifier()).getHref());

		if (!allServicesReady) {
			setupProjectServiceCallback(project.getIdentifier());
		}

		popupPanel.setProject(project);
	}

	// REVIEW this should probably be in the presenter
	// 15 second delay, in milliseconds.
	private static int SERVICE_PROVISION_POLL_DELAY = 15000;

	private void setupProjectServiceCallback(final String projectIdentifier) {

		Timer timer = new Timer() {

			@Override
			public void run() {
				if (!stillOnPage(projectIdentifier)) {
					return;
				}

				AppGinjector.get.instance().getEventBus().fireEvent(new ClearCacheEvent());
				AppGinjector.get.instance().getDispatchService()
						.execute(new GetProjectAction(projectIdentifier), new AsyncCallbackSupport<GetProjectResult>() {
							@Override
							protected void success(GetProjectResult result) {
								if (!stillOnPage(projectIdentifier)) {
									return;
								}
								// Re-set our project to trigger recalculation of current service availability.
								setProject(result.get());
							}
						});
			}
		};

		timer.schedule(SERVICE_PROVISION_POLL_DELAY);
	}

	private boolean stillOnPage(String projectId) {
		return projectId.equals(project.getIdentifier()) && isAttached() && isVisible();
	}

	private void hideAllServiceLinks() {
		tasks.setVisible(false);
		wiki.setVisible(false);
		deployments.setVisible(false);
		builds.setVisible(false);
	}

	private void setServiceLinkEnabled(boolean isEnabled, Anchor link, String href) {
		link.setVisible(true);
		if (isEnabled) {
			link.setHref(href);
			link.removeStyleName(DISABLED_STYLE);
		} else {
			// Wipe out our href attribute - setting it to null or blank causes it to redirect to the homepage.
			link.getElement().removeAttribute("href");
			link.addStyleName(DISABLED_STYLE);
		}
	}

	@UiHandler("options")
	public void showMenu(ClickEvent e) {
		if (popupPanel.isShowing()) {
			popupPanel.hide();
		} else {
			popupPanel.showRelativeTo(options);
		}
	}
}
