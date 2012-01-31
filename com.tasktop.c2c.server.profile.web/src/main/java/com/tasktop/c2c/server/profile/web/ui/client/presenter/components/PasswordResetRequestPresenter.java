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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.profile.web.client.ValidationUtils;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.RequestPasswordResetPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.PasswordResetRequestView;

public class PasswordResetRequestPresenter extends AbstractProfilePresenter {

	private final PasswordResetRequestView passwordResetRequestView;

	public PasswordResetRequestPresenter(PasswordResetRequestView view, RequestPasswordResetPlace place) {
		super(view);
		passwordResetRequestView = view;
	}

	@Override
	protected void bind() {
		passwordResetRequestView.submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doRequestPasswordReset();
			}
		});
	}

	private void doRequestPasswordReset() {
		final String email = passwordResetRequestView.email.getText();

		if (email == null || email.isEmpty()) {
			AppGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage("Please enter an email"));
			return;
		} else if (!ValidationUtils.isValidEmail(email)) {
			AppGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage("Please enter a valid email"));
			return;
		}

		getProfileService().requestPasswordReset(email, new AsyncCallbackSupport<Boolean>() {
			@Override
			public void success(Boolean result) {
				onSuccessfulRequest(email);
			}

			@Override
			public void onFailure(Throwable exception) {
				if (exception instanceof NoSuchEntityException) {
					// Show the same message even if we don't find the email. Don't reveal who is actually a user.
					onSuccessfulRequest(email);
				} else {
					super.onFailure(exception);
				}
			}
		});
	}

	private void onSuccessfulRequest(String email) {
		passwordResetRequestView.email.setText("");
		SignInPlace
				.createPlace()
				.displayOnArrival(
						Message.createSuccessMessage("Password reset instructions have been sent to your email.")).go();
	}
}
