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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class ProjectAdminKeywordsDisplayView extends Composite implements
		IProjectAdminKeywordsView<IProjectAdminKeywordsView.ProjectAdminKeywordsDisplayPresenter> {

	interface ProjectAdminKeywordsDisplayViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminKeywordsDisplayView> {
	}

	private static ProjectAdminKeywordsDisplayViewUiBinder ourUiBinder = GWT
			.create(ProjectAdminKeywordsDisplayViewUiBinder.class);

	private static ProjectAdminKeywordsDisplayView instance;

	public static ProjectAdminKeywordsDisplayView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminKeywordsDisplayView();
		}
		return instance;
	}

	private ProjectAdminKeywordsDisplayView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	@UiField
	Anchor deleteKeyword;
	@UiField
	Anchor editKeyword;
	@UiField
	Label keywordName;
	@UiField
	Label keywordDescription;

	private ProjectAdminKeywordsDisplayPresenter presenter;

	public void setPresenter(ProjectAdminKeywordsDisplayPresenter presenter) {
		this.presenter = presenter;
		keywordName.setText(presenter.getSelectedKeyword().getName());
		keywordDescription.setText(presenter.getSelectedKeyword().getDescription());
	}

	@UiHandler("editKeyword")
	void onEdit(ClickEvent event) {
		presenter.onEditKeyword();
	}

	@UiHandler("deleteKeyword")
	void onDelete(ClickEvent event) {
		presenter.onDeleteKeyword();
	}
}
