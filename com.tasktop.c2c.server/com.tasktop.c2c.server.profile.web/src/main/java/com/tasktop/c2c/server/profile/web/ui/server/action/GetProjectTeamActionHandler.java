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
package com.tasktop.c2c.server.profile.web.ui.server.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.web.shared.Profile;
import com.tasktop.c2c.server.profile.web.shared.ProjectRole;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamSummary;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectTeamResult;
import com.tasktop.c2c.server.profile.web.ui.server.WebDomain;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class GetProjectTeamActionHandler extends
		AbstractProfileActionHandler<GetProjectTeamAction, GetProjectTeamResult> {

	@Override
	public GetProjectTeamResult execute(GetProjectTeamAction action, ExecutionContext context) throws DispatchException {
		try {
			setTenancyContext(action.getProjectId());
			com.tasktop.c2c.server.profile.domain.internal.Project project;
			project = profileService.getProjectByIdentifier(action.getProjectId());

			ProjectTeamSummary summary = new ProjectTeamSummary();
			summary.setApplication(WebDomain.copy(project));
			summary.setRoles(new HashSet<ProjectRole>());

			String currentUser = Security.getCurrentUser();

			List<ProjectTeamMember> teamMemberList = new ArrayList<ProjectTeamMember>();
			for (ProjectProfile projectProfile : project.getProjectProfiles()) {
				// if the profile only has the Community role then they are just watching and don't appear in the team
				// list.
				Set<ProjectRole> profileRoles = new HashSet<ProjectRole>();

				if (projectProfile.getUser()) {
					profileRoles.add(ProjectRole.MEMBER);
				}

				if (projectProfile.getOwner()) {
					profileRoles.add(ProjectRole.OWNER);
				}

				if (profileRoles.size() == 0) {
					// We had a user with only the community role - skip them, as they aren't really a part of the
					// project
					// team (they are just watching the project).
					continue;
				}

				Profile domainProfile = WebDomain.copy(projectProfile.getProfile());

				ProjectTeamMember projectTeamMember = new ProjectTeamMember();
				projectTeamMember.setProfile(domainProfile);
				projectTeamMember.setRoles(profileRoles);

				// If this is the current user, copy our roles into the summary directly.
				// FIXME do we need this? Isn't the current user's role info already present in the UI?
				if (domainProfile.getUsername().equals(currentUser)) {
					summary.setRoles(projectTeamMember.getRoles());
				}
				teamMemberList.add(projectTeamMember);
			}

			Collections.sort(teamMemberList);
			summary.setMembers(teamMemberList);

			return new GetProjectTeamResult(summary);
		} catch (EntityNotFoundException e) {
			handle(e);
		}
		throw new IllegalStateException();
	}

}
