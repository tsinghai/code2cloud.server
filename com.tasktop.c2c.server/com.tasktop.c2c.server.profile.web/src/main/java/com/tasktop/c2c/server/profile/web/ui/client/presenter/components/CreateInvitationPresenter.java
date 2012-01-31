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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.CreateInvitationView;

public class CreateInvitationPresenter extends AbstractProfilePresenter {

	private final CreateInvitationView view;

	public CreateInvitationPresenter(CreateInvitationView invitationView) {
		super(invitationView);
		this.view = invitationView;
	}

	@Override
	protected void bind() {
		view.sendEmail.setValue(true);
		view.invitationRecipients.setText("");
		view.createInvitationsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doCreateInvitations();
			}
		});
	}

	private void doCreateInvitations() {
		view.invitations.clear();
		getProfileService().createSignUpTokensFromCsv(view.invitationRecipients.getText(), view.sendEmail.getValue(),
				new AsyncCallbackSupport<SignUpTokens>() {

					@Override
					protected void success(SignUpTokens result) {
						String output = "First Name, Last Name, Email Address, Token, URL\n";
						for (SignUpToken token : result.getTokens()) {
							output += token.getFirstname() + ", " + token.getLastname() + ", " + token.getEmail()
									+ ", " + token.getToken() + ", " + token.getUrl() + "\n";
						}
						view.invitations.clear();
						view.invitations.add(new HTMLPanel("<pre>\n" + output + "\n</pre>"));
						view.invitationRecipients.setText("");
						ProfileGinjector.get
								.instance()
								.getNotifier()
								.displayMessage(
										Message.createSuccessMessage("Invitations created.  See below for details:"));
					}
				});
	}
}
