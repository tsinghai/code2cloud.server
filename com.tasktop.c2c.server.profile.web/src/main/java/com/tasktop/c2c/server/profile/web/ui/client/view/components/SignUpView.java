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


import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignUpPresenter;

/**
 * @author jhickey
 * 
 */
public interface SignUpView extends IsWidget {

	void setPresenter(SignUpPresenter presenter);

	void setGitHubProfileData(Profile profileData);

	void setSignUpToken(SignUpToken token);

	void setProjectInvitationToken(ProjectInvitationToken token);

	void showSignUpInviteOnlyMessage();

	HasValue<String> getUsername();

	HasValue<String> getEmail();

	HasValue<String> getFirstName();

	HasValue<String> getLastName();

	HasValue<String> getPassword();

	HasValue<String> getPasswordConfirm();

	void clearErrors();

	void showPassMatchError();
}
