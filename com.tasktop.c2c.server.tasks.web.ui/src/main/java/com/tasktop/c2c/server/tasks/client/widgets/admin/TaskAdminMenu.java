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
package com.tasktop.c2c.server.tasks.client.widgets.admin;


import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminCustomFieldsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminIterationsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminKeywordsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminProductsPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminTasksPlace;

public class TaskAdminMenu extends Composite {
	interface Binder extends UiBinder<HTMLPanel, TaskAdminMenu> {
	}

	private static TaskAdminMenu instance;

	public static TaskAdminMenu getInstance() {
		if (instance == null) {
			instance = new TaskAdminMenu();
		}
		return instance;
	}

	private static Binder ourUiBinder = GWT.create(Binder.class);
	private static String SELECTED = "selected";

	@UiField
	protected Anchor keywordsAdminAnchor;
	@UiField
	protected Anchor productsAdminAnchor;
	@UiField
	protected Anchor generalAdminAnchor;
	@UiField
	protected Anchor iterationsAdminAnchor;
	@UiField
	protected Anchor customFieldsAdminAnchor;

	private Widget selected = null;

	public TaskAdminMenu() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public void setProject(Project project) {
		String projectIdentifier = project.getIdentifier();

		generalAdminAnchor.setVisible(false);
		iterationsAdminAnchor.setHref(ProjectAdminIterationsPlace.createPlace(projectIdentifier).getHref());
		keywordsAdminAnchor.setHref(ProjectAdminKeywordsPlace.createPlace(projectIdentifier).getHref());
		productsAdminAnchor.setHref(ProjectAdminProductsPlace.createPlace(projectIdentifier).getHref());
		customFieldsAdminAnchor.setHref(ProjectAdminCustomFieldsPlace.createPlace(projectIdentifier).getHref());

	}

	public void select(Place place) {
		if (place instanceof ProjectAdminKeywordsPlace) {
			changeSelected(keywordsAdminAnchor);
		} else if (place instanceof ProjectAdminProductsPlace) {
			changeSelected(productsAdminAnchor);
		} else if (place instanceof ProjectAdminTasksPlace) {
			changeSelected(generalAdminAnchor);
		} else if (place instanceof ProjectAdminIterationsPlace) {
			changeSelected(iterationsAdminAnchor);
		} else if (place instanceof ProjectAdminCustomFieldsPlace) {
			changeSelected(customFieldsAdminAnchor);
		}
	}

	private void changeSelected(Widget toSelect) {
		if (selected != null) {
			selected.removeStyleName(SELECTED);
		}
		selected = toSelect;
		selected.addStyleName(SELECTED);
	}
}
