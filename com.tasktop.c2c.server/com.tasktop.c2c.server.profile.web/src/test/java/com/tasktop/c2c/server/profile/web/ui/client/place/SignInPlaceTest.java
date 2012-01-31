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
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class SignInPlaceTest extends AbstractPlaceTest {

	@Test
	public void testBasicSignin() {
		mockDispatchResult(new GetUserInfoResult(getAnonUserInfo()));

		SignInPlace signInPlace = SignInPlace.createPlace();
		signInPlace.go();

		Assert.assertFalse(appGinjector.getAppState().hasPendingAgreements());
		verify(placeController).goTo(signInPlace);
	}

	@Test
	public void testSigninWhenAlreadySignedin() {
		mockDispatchResult(new GetUserInfoResult(getRegularUserInfo()));

		SignInPlace signInPlace = SignInPlace.createPlace();
		signInPlace.go();

		Assert.assertFalse(appGinjector.getAppState().hasPendingAgreements());
		verify(placeController).goTo(isA(ProjectsDiscoverPlace.class));
	}
}
