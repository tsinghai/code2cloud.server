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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.team;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.web.shared.ProjectRole;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;

public class ProjectTeamAdminMemberView extends Composite {

	interface Binder extends UiBinder<Widget, ProjectTeamAdminMemberView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label name;
	@UiField
	Label username;
	@UiField
	Label email;
	@UiField
	Label role;
	@UiField
	ListBox roleListBox;

	@UiField
	Anchor removeButton;

	private ProjectTeamMember member;

	public ProjectTeamAdminMemberView(ProjectTeamMember member, boolean isSelf) {
		this.member = member;
		initWidget(uiBinder.createAndBindUi(this));
		roleListBox.addItem("Member");
		roleListBox.addItem("Owner + Member");

		this.name.setText(member.getProfile().getFirstName() + " " + member.getProfile().getLastName());
		this.username.setText(member.getProfile().getUsername());
		this.email.setText(member.getProfile().getEmail()); // ALLOWED????

		String roleText = getRole(member.getRoles());

		if (isSelf) {
			this.role.setText(roleText);
			this.roleListBox.setVisible(false);
			this.removeButton.setVisible(false);
		} else {
			this.roleListBox.setSelectedIndex(member.getRoles().size() - 1); // hack
			this.role.setVisible(false);
		}
	}

	/**
	 * @param roles
	 * @return
	 */
	private String getRole(Set<ProjectRole> roles) {
		String highestRole = null;
		for (ProjectRole role : roles) {
			if (highestRole == null) {
				highestRole = role.getLabel();
			} else if (role.getLabel().equals("Owner")) {
				highestRole = "Owner + Member";
			}
		}
		return highestRole;
	}

	/**
	 * @return
	 */
	public ProjectTeamMember getMember() {
		Set<ProjectRole> roles = new HashSet<ProjectRole>();
		switch (roleListBox.getSelectedIndex()) {
		case 0:
			roles.add(ProjectRole.MEMBER);
			break;
		case 1:
			roles.add(ProjectRole.MEMBER);
			roles.add(ProjectRole.OWNER);
			break;
		}
		member.setRoles(roles);
		return member;
	}

}
