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

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.event.LeaveProjectClickHandler;
import com.tasktop.c2c.server.profile.web.ui.client.event.UnwatchProjectClickHandler;
import com.tasktop.c2c.server.profile.web.ui.client.event.WatchProjectClickHandler;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;

public class ProjectOptionsPopupPanel extends PopupPanel {

	interface ProjectOptionsPopupPanelUiBinder extends UiBinder<Widget, ProjectOptionsPopupPanel> {
	}

	private static ProjectOptionsPopupPanelUiBinder uiBinder = GWT.create(ProjectOptionsPopupPanelUiBinder.class);

	@UiField
	Anchor watchLink;
	@UiField
	Anchor unwatchLink;
	@UiField
	Anchor leaveLink;
	@UiField
	Anchor adminLink;
	@UiField
	LIElement watchWrapper;
	@UiField
	LIElement unwatchWrapper;
	@UiField
	LIElement leaveWrapper;
	@UiField
	LIElement adminWrapper;
	@UiField
	UListElement wrapperList;

	public ProjectOptionsPopupPanel() {
		super(true);

		setWidget(uiBinder.createAndBindUi(this));
		AppGinjector.get.instance().getEventBus().addHandler(ScrollEvent.getType(), new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				hide();
			}
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				hide();
			}
		});
	}

	private List<HandlerRegistration> removalList = new ArrayList<HandlerRegistration>();

	public void setProject(Project project) {

		// Clear out any previous handlers.
		for (HandlerRegistration reg : removalList) {
			reg.removeHandler();
		}
		removalList.clear();

		final String projId = project.getIdentifier();
		final EventBus bus = ProfileEntryPoint.getInstance().getEventBus();

		adminLink.setHref(ProjectAdminSettingsPlace.createPlace(projId).getHref());

		removalList.add(watchLink.addClickHandler(new WatchProjectClickHandler(project) {

			@Override
			protected void onWatchSuccess(Project project) {
				// Re-set the project to trigger a menu re-render.
				ProjectOptionsPopupPanel.this.setProject(project);
				ProfileGinjector.get.instance().getNotifier()
						.displayMessage(Message.createSuccessMessage("Watching project " + project.getName()));
			}
		}));

		removalList.add(unwatchLink.addClickHandler(new UnwatchProjectClickHandler(project) {

			@Override
			protected void onUnwatchSuccess(Project project) {
				// Re-set the project to trigger a menu re-render.
				ProjectOptionsPopupPanel.this.setProject(project);
				ProfileGinjector.get.instance().getNotifier()
						.displayMessage(Message.createSuccessMessage("Unwatched project " + project.getName()));
			}
		}));

		removalList.add(leaveLink.addClickHandler(new LeaveProjectClickHandler(project) {

			@Override
			protected void onLeaveSuccess(Project project) {
				// Navigate to the "My Projects" page.
				ProjectsDiscoverPlace.createPlace()
						.displayOnArrival(Message.createSuccessMessage("Left project " + project.getName())).go();
			}
		}));

		// Make sure all of our elements are removed before we reconstruct the menu.
		adminWrapper.removeFromParent();
		watchWrapper.removeFromParent();
		unwatchWrapper.removeFromParent();
		leaveWrapper.removeFromParent();

		// Now, highlight and activate only the appropriate links for this user.
		if (AuthenticationHelper.isAdmin(projId)) {
			// Show the leave and administration links
			wrapperList.appendChild(leaveWrapper);
			wrapperList.appendChild(adminWrapper);
		} else if (AuthenticationHelper.isCommitter(projId)) {
			wrapperList.appendChild(leaveWrapper);
		} else if (AuthenticationHelper.isWatching(projId)) {
			wrapperList.appendChild(unwatchWrapper);
		} else {
			// No roles, so the only thing available is to watch if this user can see this project.
			wrapperList.appendChild(watchWrapper);
		}
	}
}
