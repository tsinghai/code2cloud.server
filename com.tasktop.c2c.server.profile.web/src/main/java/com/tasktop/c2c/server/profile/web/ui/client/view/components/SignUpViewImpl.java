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


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectInvitationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignUpPresenter;

public class SignUpViewImpl extends AbstractComposite implements SignUpView {

	private static SignUpViewImpl instance = null;

	public static SignUpViewImpl getInstance() {
		if (instance == null) {
			instance = new SignUpViewImpl();
		}
		return instance;
	}

	interface SignUpWithTokenViewUiBinder extends UiBinder<Widget, SignUpViewImpl> {
	}

	private static SignUpWithTokenViewUiBinder uiBinder = GWT.create(SignUpWithTokenViewUiBinder.class);

	public interface SignUpConstants extends Constants {

		@DefaultStringValue("has invited you to collaborate on a project")
		String userHasInvitedToCollaborate();

		@DefaultStringValue("Account Information")
		String accountInformation();
	}

	public interface SignUpMessages extends Messages {
		@DefaultMessage("GitHub Account \"{0}\" Successfully Linked")
		String githubAccountLinkedSuccessfully(String username);
	}

	public interface HtmlTemplate extends SafeHtmlTemplates {

		@Template("{0} {1} {2}")
		SafeHtml issuingUser(String firstName, String lastName, String userHasInvitedToCollaborateConstant);
	}

	private final SignUpConstants CONSTANTS = GWT.create(SignUpConstants.class);
	private final SignUpMessages MESSAGES = GWT.create(SignUpMessages.class);
	private final HtmlTemplate TEMPLATE = GWT.create(HtmlTemplate.class);

	@UiField
	HTMLPanel signUpForm;
	@UiField
	TextBox username;
	@UiField
	TextBox email;
	@UiField
	TextBox firstName;
	@UiField
	TextBox lastName;
	@UiField
	PasswordTextBox password;
	@UiField
	PasswordTextBox passwordConfirm;
	@UiField
	Button createAccountButton;

	@UiField
	Button githubButton;
	@UiField(provided = true)
	FormPanel githubForm;
	@UiField
	Panel githubLinkedPanel;
	@UiField
	Panel linkGithubPanel;
	@UiField
	Label githubLinkedLabel;

	@UiField
	Panel messagePanel;
	@UiField
	Panel passMatchError;
	@UiField
	Panel issuingUserHeader;
	@UiField
	Label issuingUser;
	@UiField
	Anchor signInAnchor;

	private SignUpPresenter presenter;

	private SignUpViewImpl() {
		// Give our Github form a target of "_self" - that will ensure that it replaces the current page when the
		// redirect to GitHub happens (which is what we want).
		this.githubForm = new FormPanel(new NamedFrame("_self"));
		initWidget(uiBinder.createAndBindUi(this));
		hookDefaultButton(createAccountButton);

	}

	@UiHandler("githubButton")
	void submitForm(ClickEvent event) {
		githubForm.submit();
	}

	@UiHandler("createAccountButton")
	void signup(ClickEvent event) {
		if (this.presenter != null) {
			presenter.signup();
		}
	}

	@Override
	public void setPresenter(SignUpPresenter presenter) {
		this.presenter = presenter;
		reset();

	}

	private void reset() {
		username.setValue(null);
		firstName.setValue(null);
		lastName.setValue(null);
		email.setValue(null);
		password.setValue(null);
		passwordConfirm.setValue(null);
		signUpForm.setVisible(true);
		createAccountButton.setVisible(true);
		issuingUserHeader.setVisible(false);
		messagePanel.setVisible(false);
		passMatchError.setVisible(false);
	}

	public void setGitHubProfileData(Profile profileData) {
		if (profileData != null) {
			githubLinkedLabel.setText(MESSAGES.githubAccountLinkedSuccessfully(profileData.getUsername()));
			linkGithubPanel.setVisible(false);
			githubLinkedPanel.setVisible(true);
			username.setText(profileData.getUsername());
			email.setText(profileData.getEmail());
			firstName.setText(profileData.getFirstName());
			lastName.setText(profileData.getLastName());
		}
	}

	public void setSignUpToken(SignUpToken token) {
		if (token != null) {
			email.setText(token.getEmail());
			firstName.setText(token.getFirstname());
			lastName.setText(token.getLastname());
		}

		signUpForm.setVisible(true);
		createAccountButton.setVisible(true);
		messagePanel.setVisible(false);
		issuingUserHeader.setVisible(false);
	}

	public void setProjectInvitationToken(ProjectInvitationToken token) {
		email.setText(token.getEmail());
		if (token.getIssuingUser() != null) {
			String issuingUserStr = TEMPLATE.issuingUser(token.getIssuingUser().getFirstName(),
					token.getIssuingUser().getLastName(), CONSTANTS.userHasInvitedToCollaborate()).asString();
			issuingUser.setText(issuingUserStr);
			issuingUserHeader.setVisible(true);
		}
		signInAnchor.setHref(SignInPlace.createPlace(ProjectInvitationPlace.createPlace(token.getToken())).getHref());
		signUpForm.setVisible(true);
		createAccountButton.setVisible(true);
		messagePanel.setVisible(false);
	}

	public void showSignUpInviteOnlyMessage() {
		signUpForm.setVisible(false);
		createAccountButton.setVisible(false);
		issuingUserHeader.setVisible(false);
		messagePanel.setVisible(true);
	}

	public void showPassMatchError() {
		passMatchError.setVisible(true);
	}

	public void clearErrors() {
		passMatchError.setVisible(false);
	}

	@Override
	public HasValue<String> getUsername() {
		return username;
	}

	@Override
	public HasValue<String> getEmail() {
		return email;
	}

	@Override
	public HasValue<String> getFirstName() {
		return firstName;
	}

	@Override
	public HasValue<String> getLastName() {
		return lastName;
	}

	@Override
	public HasValue<String> getPassword() {
		return password;
	}

	@Override
	public HasValue<String> getPasswordConfirm() {
		return passwordConfirm;
	}

}
