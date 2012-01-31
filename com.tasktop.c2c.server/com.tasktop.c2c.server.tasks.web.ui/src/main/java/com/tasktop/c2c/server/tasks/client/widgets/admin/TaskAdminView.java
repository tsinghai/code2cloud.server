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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.tasktop.c2c.server.profile.domain.project.Project;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskAdminView extends Composite {
	interface Binder extends UiBinder<HTMLPanel, TaskAdminView> {
	}

	private static Binder binder = GWT.create(Binder.class);

	private static TaskAdminView instance;

	public static TaskAdminView getInstance() {
		if (instance == null) {
			instance = new TaskAdminView();
		}
		return instance;
	}

	@UiField
	protected TaskAdminMenu menu;
	@UiField
	protected SimplePanel contentContainer;

	private TaskAdminView() {
		initWidget(binder.createAndBindUi(this));
	}

	/**
	 * @return the contentContainer
	 */
	public SimplePanel getContentContainer() {
		return contentContainer;
	}

	public void setProject(Project project) {
		menu.setProject(project);
	}

	public void selectPlace(Place place) {
		menu.select(place);
	}

}
