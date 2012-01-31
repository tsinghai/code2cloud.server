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
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Project;

public class ProjectInvitationView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, ProjectInvitationView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private Project project;

	@UiField
	Label projectName;
	@UiField
	ParagraphElement projectDescription;
	@UiField
	public Anchor acceptInviteAnchor;

	public ProjectInvitationView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	private void updateUi() {
		projectName.setText(project.getName());
		projectDescription.setInnerText(SafeHtmlUtils.htmlEscape(project.getDescription()));
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;

		updateUi();
	}
}
