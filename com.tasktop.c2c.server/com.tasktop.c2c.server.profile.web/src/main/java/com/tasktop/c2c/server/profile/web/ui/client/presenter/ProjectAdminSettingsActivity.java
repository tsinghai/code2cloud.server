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
package com.tasktop.c2c.server.profile.web.ui.client.presenter;


import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.shared.actions.UpdateProjectAction;
import com.tasktop.c2c.server.profile.web.shared.actions.UpdateProjectResult;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.settings.ProjectAdminSettingsView;

public class ProjectAdminSettingsActivity extends AbstractActivity implements ProjectAdminSettingsView.Presenter,
		SplittableActivity {

	private boolean editing = false;
	private Project project;
	private ProjectAdminSettingsView view = ProjectAdminSettingsView.getInstance();

	public ProjectAdminSettingsActivity() {
	}

	public void setPlace(Place p) {
		ProjectAdminSettingsPlace place = (ProjectAdminSettingsPlace) p;
		this.project = place.getProject();
		updateView();
	}

	private void updateView() {
		view.setPresenter(this);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void onCancel() {
		super.onCancel();
	}

	@Override
	public void onEdit() {
		editing = true;
		updateView();
	}

	@Override
	public void onSaveProject() {
		AppGinjector.get.instance().getDispatchService()
				.execute(new UpdateProjectAction(project), new AsyncCallbackSupport<UpdateProjectResult>() {
					@Override
					protected void success(UpdateProjectResult result) {
						editing = false;
						project = result.get();
						updateView();
						ProfileGinjector.get.instance().getNotifier()
								.displayMessage(Message.createSuccessMessage("Project saved"));
					}
				});
	}

	@Override
	public void onCancelProjectEdit() {
		editing = false;
		updateView();
	}

	@Override
	public void start(final AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
	}

	@Override
	public boolean isEditing() {
		return editing;
	}
}
