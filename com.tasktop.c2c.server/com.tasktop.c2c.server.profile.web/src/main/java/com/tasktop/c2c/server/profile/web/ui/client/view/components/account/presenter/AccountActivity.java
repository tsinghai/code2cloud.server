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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter;

import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.place.UserAccountPlace;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.AccountView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.place.AccountProfilePlace;

public class AccountActivity extends AbstractActivity implements IAccountView.AccountPresenter, SplittableActivity {

	private Profile profile;
	private List<SshPublicKey> sshPublicKeys;
	private SshPublicKey selectedSshKey;
	// This is here as a substitute for PlaceController.getWhere() which would return the current place and views
	// to configure themselves from the place information
	private Place where;
	private String originalEmail;

	public AccountActivity() {
	}

	public void setPlace(Place p) {
		UserAccountPlace place = (UserAccountPlace) p;
		this.profile = place.getProfile();
		this.sshPublicKeys = place.getSshPublicKeys();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(AccountView.getInstance());
		goTo(new AccountProfilePlace());
	}

	private void updateView() {
		AccountView.getInstance().goTo(where);
	}

	@Override
	public void goTo(Place place) {
		if (place == null) {
			place = new AccountProfilePlace();
		}
		where = place;
		AccountView.getInstance().setPresenter(this);
		AccountView.getInstance().goTo(place);
	}

	@Override
	public Place getWhere() {
		return where;
	}

	@Override
	public List<SshPublicKey> getSshKeys() {
		return sshPublicKeys;
	}

	@Override
	public void saveChangedPassword(String oldPassword, final String newPassword, String newPasswordConfirmation,
			final ClientCallback<Boolean> callback) {
		// First, if we're selecting a new product then clear out any old messages.
		ProfileGinjector.get.instance().getNotifier().clearMessages();
		if (!newPassword.equals(newPasswordConfirmation)) {
			ProfileGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage("New Password and Confirm Password must be the same."));
		} else if (!(newPassword.length() > 0)) {
			ProfileGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage("Please provide a password."));
		} else {
			// verify old password
			ProfileEntryPoint.getInstance().getProfileService()
					.logon(profile.getUsername(), oldPassword, false, new AsyncCallbackSupport<Credentials>() {

						@Override
						public void success(Credentials result) {
							updatePassword(newPassword, callback);
						}

						@Override
						public void onFailure(Throwable result) {
							ProfileGinjector.get
									.instance()
									.getNotifier()
									.displayMessage(
											Message.createErrorMessage("The current password you gave is incorrect."));
						}
					});
		}
	}

	private void updatePassword(final String newPassword, final ClientCallback<Boolean> callback) {
		profile.setPassword(newPassword);
		ProfileEntryPoint.getInstance().getProfileService()
				.updateProfile(profile, new AsyncCallbackSupport<Credentials>() {
					@Override
					public void success(Credentials result) {
						if (result != null) {
							ProfileEntryPoint.getInstance().getAppState().setCredentials(result);
							ProfileGinjector.get.instance().getNotifier()
									.displayMessage(Message.createSuccessMessage("Password updated."));
							callback.onReturn(true);
						}
					}

					@Override
					public void onFailure(Throwable exception) {
						super.onFailure(exception);
						callback.onReturn(false);
					}
				});
		// set password back to null so future profile updates won't include it
		profile.setPassword(null);
	}

	@Override
	public void saveSshKey(ErrorCapableView errorView) {
		// TODO: No server side method
		if (selectedSshKey.getId() == null) {
			createSshKey(errorView, selectedSshKey);
		} else {
			updateSshKey(errorView, selectedSshKey);
		}
	}

	private void createSshKey(ErrorCapableView errorView, SshPublicKey sshPublicKey) {
		SshPublicKeySpec sshPublicKeySpec = new SshPublicKeySpec(sshPublicKey.getName(), sshPublicKey.getKeyText());
		ProfileEntryPoint.getInstance().getProfileService()
				.createSshPublicKey(sshPublicKeySpec, new AsyncCallbackSupport<SshPublicKey>(errorView) {
					@Override
					protected void success(SshPublicKey result) {
						sshPublicKeys.add(result);
						selectedSshKey = null;
						updateView();
						ProfileGinjector.get
								.instance()
								.getNotifier()
								.displayMessage(
										Message.createSuccessMessage("Public Key saved: " + result.getName() + " ("
												+ result.getFingerprint() + ")"));
					}
				});
	}

	private void updateSshKey(ErrorCapableView errorView, SshPublicKey sshPublicKey) {
		AppGinjector.get.instance().getDispatchService();
		ProfileEntryPoint.getInstance().getProfileService()
				.updateSshPublicKey(sshPublicKey, new AsyncCallbackSupport<SshPublicKey>(errorView) {
					@Override
					protected void success(SshPublicKey result) {
						selectedSshKey = null;
						updateView();
						ProfileGinjector.get
								.instance()
								.getNotifier()
								.displayMessage(
										Message.createSuccessMessage("Public Key updated: " + result.getName() + " ("
												+ result.getFingerprint() + ")"));
					}
				});
	}

	@Override
	public void deleteSshKey(final Long sshPublicKeyId) {
		ProfileEntryPoint.getInstance().getProfileService()
				.removeSshPublicKey(sshPublicKeyId, new AsyncCallbackSupport<Void>() {
					@Override
					protected void success(Void result) {
						for (SshPublicKey sshPublicKey : sshPublicKeys) {
							if (sshPublicKey.getId().equals(sshPublicKeyId)) {
								sshPublicKeys.remove(sshPublicKey);
								break;
							}
						}
						updateView();
					}
				});
	}

	@Override
	public void selectSshKey(SshPublicKey sshPublicKey) {
		selectedSshKey = sshPublicKey;
	}

	@Override
	public SshPublicKey getSelectedSshKey() {
		return selectedSshKey;
	}

	@Override
	public Profile getProfile() {
		return profile;
	}

	@Override
	public void saveProfile() {
		ProfileEntryPoint.getInstance().getProfileService()
				.updateProfile(profile, new AsyncCallbackSupport<Credentials>() {
					@Override
					public void success(Credentials result) {
						ProfileEntryPoint.getInstance().getAppState().setCredentials(result);

						profile = result.getProfile();
						updateView();
						ProfileGinjector.get.instance().getNotifier()
								.displayMessage(Message.createSuccessMessage("Profile updated."));
						if (!originalEmail.equals(profile.getEmail())) {
							ProfileGinjector.get
									.instance()
									.getNotifier()
									.displayMessage(
											Message.createSuccessMessage("Verification email sent. Please check your email to confirm your address."));
							originalEmail = profile.getEmail();
						}
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView.
	 * AccountProfilePresenter#verifyEmail()
	 */
	@Override
	public void verifyEmail() {
		ProfileEntryPoint.getInstance().getProfileService().verifyEmail(new AsyncCallbackSupport<Void>() {
			@Override
			public void success(Void result) {

				ProfileGinjector.get
						.instance()
						.getNotifier()
						.displayMessage(
								Message.createSuccessMessage("Verification email sent. Please check your email to confirm your address."));
			}
		});

	}
}
