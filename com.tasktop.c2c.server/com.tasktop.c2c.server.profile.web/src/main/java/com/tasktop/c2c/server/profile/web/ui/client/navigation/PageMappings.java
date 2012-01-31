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
package com.tasktop.c2c.server.profile.web.ui.client.navigation;

import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AgreementsPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.AdminProfilePlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.AppSectionPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.EmailVerificationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.HelpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.InvitationCreatorPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.NewProjectPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDashboardPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDeploymentPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectInvitationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectTeamPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.RequestPasswordResetPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ResetPasswordPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignOutPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignUpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.UserAccountPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminSourcePlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.place.ProjectAdminTeamPlace;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class PageMappings {
	public static PageMapping Discover = ProjectsDiscoverPlace.Discover;
	public static PageMapping ProjectHome = ProjectHomePlace.ProjectHome;
	public static PageMapping SignOut = SignOutPlace.SignOut;
	public static PageMapping SignUp = new PageMapping(new SignUpPlace.Tokenizer(), "signup", "signup/{"
			+ SignUpPlace.TOKEN + "}");
	public static PageMapping SignIn = SignInPlace.SignIn;
	public static PageMapping ProjectDashboard = new PageMapping(new ProjectDashboardPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/dashboard");
	public static PageMapping ProjectDeployment = new PageMapping(new ProjectDeploymentPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/deployments");
	public static PageMapping NewProject = new PageMapping(new NewProjectPlace.Tokenizer(), "newProject");
	public static PageMapping Help = new PageMapping(new HelpPlace.Tokenizer(), "help");
	// Logout= new PageMapping(new SignOutPlace.Tokenizer(), "signout");
	public static PageMapping RequestPasswordReset = new PageMapping(new RequestPasswordResetPlace.Tokenizer(),
			"requestPasswordReset");
	public static PageMapping Account = new PageMapping(new UserAccountPlace.Tokenizer(), "account");
	public static PageMapping ProjectTeam = new PageMapping(new ProjectTeamPlace.Tokenizer(), Path.PROJECT_BASE + "/{"
			+ Path.PROJECT_ID + "}/team");
	public static PageMapping Agreements = AgreementsPlace.Agreements;
	public static PageMapping ProjectAdminSCM = new PageMapping(new ProjectAdminSourcePlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/admin/scm");
	public static PageMapping ProjectAdminTeam = new PageMapping(new ProjectAdminTeamPlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/admin/team");
	public static PageMapping VerifyEmail = new PageMapping(new EmailVerificationPlace.Tokenizer(), "verifyEmail/{"
			+ SignUpPlace.TOKEN + "}");
	public static PageMapping ResetPassword = new PageMapping(new ResetPasswordPlace.Tokenizer(), "resetPassword/{"
			+ SignUpPlace.TOKEN + "}");
	public static PageMapping ProjectInvitation = new PageMapping(new ProjectInvitationPlace.Tokenizer(),
			"invitation/{" + SignUpPlace.TOKEN + "}");
	public static PageMapping AppSection = new PageMapping(new AppSectionPlace.Tokenizer(), Path.PROJECT_BASE + "/{"
			+ Path.PROJECT_ID + "}/section/{" + AppSectionPlace.SECTION + "}");
	public static PageMapping InvitationCreator = new PageMapping(new InvitationCreatorPlace.Tokenizer(),
			"admin/invitationCreator");
	public static PageMapping AdminProfiles = new PageMapping(new AdminProfilePlace.Tokenizer(), "admin/profile");
	public static PageMapping ProjectAdmin = ProjectAdminSettingsPlace.ProjectAdminSettings;

}
