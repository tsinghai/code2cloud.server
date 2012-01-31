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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.BatchResult;

import org.junit.Test;

import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.web.client.place.AgreementsPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.shared.actions.GetPendingAgreementsResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoResult;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectAdminSettingsPlace;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class ProjectAdminSettingsPlaceTest extends AbstractPlaceTest {

	@Test
	public void testRequiresAuth() {
		mockDispatchResult(new GetUserInfoResult(getAnonUserInfo()), new GetProjectResult(getMockProject()));

		ProjectAdminSettingsPlace settingsPlace = ProjectAdminSettingsPlace.createPlace(projectId);

		settingsPlace.go();

		verify(placeController).goTo(isA(SignInPlace.class));
	}

	@Test
	public void testBasic() {
		mockDispatchResult(new GetUserInfoResult(getRegularUserInfo()), new GetProjectResult(getMockProject()));

		ProjectAdminSettingsPlace settingsPlace = ProjectAdminSettingsPlace.createPlace(projectId);

		settingsPlace.go();

		verify(placeController).goTo(settingsPlace);
	}

	@Test
	public void testWithPermissionsException() {
		BatchResult settingPlaceResult = getBatchResult(new ActionException(new InsufficientPermissionsException()),
				new GetUserInfoResult(getRegularUserInfo()));
		BatchResult defaultDiscoverResult = getBatchResult(new GetUserInfoResult(getRegularUserInfo()));
		mockDispatchResults(settingPlaceResult, defaultDiscoverResult);

		ProjectAdminSettingsPlace settingsPlace = ProjectAdminSettingsPlace.createPlace(projectId);

		settingsPlace.go();

		verify(placeController, never()).goTo(settingsPlace);
	}

	@Test
	public void testWithPendingAgreements() {
		UserInfo userInfo = getRegularUserInfo();
		userInfo.setHasPendingAgreements(true);

		BatchResult settingPlaceResult = getBatchResult(new GetUserInfoResult(userInfo), new GetProjectResult(
				getMockProject()));
		BatchResult agreementsResult = getBatchResult(new GetUserInfoResult(userInfo), new GetPendingAgreementsResult(
				Arrays.asList(new Agreement())));
		mockDispatchResults(settingPlaceResult, agreementsResult);

		ProjectAdminSettingsPlace settingsPlace = ProjectAdminSettingsPlace.createPlace(projectId);

		settingsPlace.go();

		verify(placeController).goTo(isA(AgreementsPlace.class));
	}
}
