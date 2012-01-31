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
import java.util.Collections;
import java.util.List;


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.shared.ProjectRole;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;

public class ProjectTeamView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, ProjectTeamView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Panel teamMembersPanel;
	@UiField
	Panel manageMembersPanel;
	@UiField
	public Anchor manageMembers;

	private CellList<ProjectTeamMember> teamMemberList;

	public ProjectTeamView() {
		initWidget(uiBinder.createAndBindUi(this));
		teamMemberList = new CellList<ProjectTeamMember>(new TeamMemberCell());
		// TODO figure out how to get rid of the cursor pointer GWT is using
		teamMemberList.setSelectionModel(new NoSelectionModel<ProjectTeamMember>());
		teamMemberList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		teamMembersPanel.add(teamMemberList);
		// TODO link to the Team Admin page
	}

	public void setProjectTeamMembers(List<ProjectTeamMember> teamMembers) {
		this.teamMemberList.setRowData(teamMembers);
	}

	public void setupAdminControls(String projectIdentifier) {
		if (AuthenticationHelper.hasRoleForProject(Role.Admin, projectIdentifier)) {
			manageMembersPanel.setVisible(true);
		} else {
			manageMembersPanel.setVisible(false);
		}
	}

	interface PageTemplate extends SafeHtmlTemplates {

		@Template("<div class=\"member-info\"><div class=\"avatar left\"><img src=\"{4}\"></div><div class=\"left\"><span class=\"name\">{0} {1}</span><span class=\"username\">{2}</span><span class=\"membership\">{3}</span></div><div class=\"clear\"></div></div>")
		SafeHtml createTeamMemberCell(SafeHtml firstName, SafeHtml lastName, SafeHtml username, SafeHtml role,
				String avatarUrl);
	}

	private static final PageTemplate TEMPLATE = GWT.create(PageTemplate.class);

	private final class TeamMemberCell extends AbstractCell<ProjectTeamMember> {

		@Override
		public void render(Context context, ProjectTeamMember value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}
			SafeHtml safeFirst = SafeHtmlUtils.fromString(value.getProfile().getFirstName());
			SafeHtml safeLast = SafeHtmlUtils.fromString(value.getProfile().getLastName());
			SafeHtml safeUsername = SafeHtmlUtils.fromString(value.getProfile().getUsername());
			List<String> roles = new ArrayList<String>();
			for (ProjectRole role : value.getRoles()) {
				roles.add(role.getLabel());
			}
			Collections.sort(roles);
			StringBuilder rolesLabel = new StringBuilder();
			for (String label : roles) {
				rolesLabel.append(label).append(", ");
			}
			rolesLabel.delete(rolesLabel.length() - 2, rolesLabel.length());
			SafeHtml safeRole = SafeHtmlUtils.fromString(rolesLabel.toString());

			String avatarUrl = Avatar.computeAvatarUrl(value.getProfile().getGravatarHash(), Avatar.Size.LARGE);

			sb.append(TEMPLATE.createTeamMemberCell(safeFirst, safeLast, safeUsername, safeRole, avatarUrl));
		}
	}

}
