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
package com.tasktop.c2c.server.tasks.client.widgets.admin.keywords;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class ProjectAdminKeywordsView extends Composite implements
		IProjectAdminKeywordsView<IProjectAdminKeywordsView.ProjectAdminKeywordsPresenter> {

	interface ProjectAdminKeywordsViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminKeywordsView> {
	}

	private static ProjectAdminKeywordsViewUiBinder ourUiBinder = GWT.create(ProjectAdminKeywordsViewUiBinder.class);

	public static interface Presenter extends ProjectAdminKeywordsMenu.Presenter {
	}

	private static ProjectAdminKeywordsView instance;
	private ProjectAdminKeywordsEditView editView = ProjectAdminKeywordsEditView.getInstance();
	private ProjectAdminKeywordsDisplayView displayView = ProjectAdminKeywordsDisplayView.getInstance();

	public static ProjectAdminKeywordsView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminKeywordsView();
		}
		return instance;
	}

	@UiField
	SimplePanel contentContainer;
	@UiField(provided = true)
	ProjectAdminKeywordsMenu menu = ProjectAdminKeywordsMenu.getInstance();

	public ProjectAdminKeywordsView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void setPresenter(IProjectAdminKeywordsView.ProjectAdminKeywordsPresenter presenter) {
		menu.setPresenter(presenter);
		if (presenter.isEditing()) {
			editView.setPresenter(presenter);
			contentContainer.setWidget(editView);
		} else {
			displayView.setPresenter(presenter);
			contentContainer.setWidget(displayView);
		}
	}
}
