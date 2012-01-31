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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.profile;


import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView.AccountProfilePresenter;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractAccountProfileView extends Composite {

	@UiField
	public Image avatarImage;
	@UiField
	public Anchor verifyEmailButton;
	@UiField
	public Anchor whatIsVerificationButton;
	public WhatIsEmailVerificationDialog whatIsEmailVerificationDialog = new WhatIsEmailVerificationDialog();
	@Ignore
	@UiField
	public Label emailVerifiedLabel;

	protected AccountProfilePresenter presenter;

	/**
	 * 
	 */
	public AbstractAccountProfileView() {
		super();
	}

	public void setPresenter(AccountProfilePresenter presenter) {
		this.presenter = presenter;
		avatarImage.setUrl(Avatar.computeAvatarUrl(presenter.getProfile().getGravatarHash(), Avatar.Size.LARGE)); // Duped
		updateEmailVerification();
	}

	/**
	 * @param presenter
	 */
	private void updateEmailVerification() {
		verifyEmailButton.setVisible(!presenter.getProfile().getEmailVerfied());
		if (presenter.getProfile().getEmailVerfied()) {
			emailVerifiedLabel.setText("Verified");
			emailVerifiedLabel.getElement().getParentElement().removeClassName("error");
			emailVerifiedLabel.getElement().getParentElement().addClassName("success");
		} else {
			emailVerifiedLabel.setText("Not Verified");
			emailVerifiedLabel.getElement().getParentElement().addClassName("error");
			emailVerifiedLabel.getElement().getParentElement().removeClassName("success");
		}
	}

	@UiHandler("verifyEmailButton")
	void onVerifyEmail(ClickEvent event) {
		presenter.verifyEmail();
	}

	@UiHandler("whatIsVerificationButton")
	void onWhatIsVerification(ClickEvent event) {
		whatIsEmailVerificationDialog.center();
	}

}
