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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;


import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEvent;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.RequestPasswordResetPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.SignInView;

public class SignInPresenter extends AbstractProfilePresenter {

	private final SignInView signinView;
	private DefaultPlace postActionPlace;

	public SignInPresenter(SignInView logonView, SignInPlace place) {
		super(logonView);
		this.signinView = logonView;
		this.postActionPlace = place.getAfterSuccessfulSignIn();
	}

	@Override
	protected void bind() {
		signinView.clearForm();
		signinView.setPresenter(this);
		signinView.requestPasswordReset.setHref(RequestPasswordResetPlace.createPlace().getHref());
		signinView.username.setFocus(true);
	}

	public void doLogon() {
		Boolean rememberMe = signinView.rememberMe.getValue();
		getProfileService().logon(signinView.username.getValue(), signinView.password.getValue(),
				rememberMe == null ? false : rememberMe, new AsyncCallbackSupport<Credentials>() {

					@Override
					public void success(Credentials result) {
						getEventBus().fireEvent(new LogonEvent(result));
						AppGinjector.get.instance().getAppState().setCredentials(result);
						if (postActionPlace != null) {
							postActionPlace.go();
						} else {
							ProjectsDiscoverPlace.createPlace().go();
						}
					}

					@Override
					public void onFailure(Throwable result) {
						signinView.password.setText("");
						signinView.username.setSelectionRange(0, signinView.username.getText().length());
						signinView.username.setFocus(true);
						super.onFailure(result);
					}
				});
	}
}
