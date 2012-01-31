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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.team;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.web.client.presenter.person.PersonUtil;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;

public class ProjectTeamAdminView extends Composite implements IProjectAdminTeamView<IProjectAdminTeamView.Presenter> {

	interface Binder extends UiBinder<Widget, ProjectTeamAdminView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private static ProjectTeamAdminView instance;

	public static ProjectTeamAdminView getInstance() {
		if (instance == null) {
			instance = new ProjectTeamAdminView();
		}
		return instance;
	}

	@UiField
	Panel teamMembersPanel;

	@UiField
	public TextBox inviteEmail;
	@UiField
	public Button inviteButton;

	private Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		setProjectTeamMembers(presenter.getTeamMembers());
	}

	public ProjectTeamAdminView() {
		initWidget(uiBinder.createAndBindUi(this));
		inviteEmail.getElement().setPropertyString("placeholder", "Enter Email Address");
	}

	private void setProjectTeamMembers(List<ProjectTeamMember> teamMembers) {
		teamMembersPanel.clear();

		for (final ProjectTeamMember member : teamMembers) {
			boolean isSelf = PersonUtil.toPerson(member.getProfile()).equals(presenter.getSelf());
			final ProjectTeamAdminMemberView ptamv = new ProjectTeamAdminMemberView(member, isSelf);
			teamMembersPanel.add(ptamv);
			ptamv.removeButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					removeMember(member, ptamv);
				}
			});
			ptamv.roleListBox.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {
					updateTeamMember(ptamv.getMember());

				}
			});
		}
	}

	private void removeMember(final ProjectTeamMember teamMember, final ProjectTeamAdminMemberView view) {
		final ConfirmRemoveMemberDialog confirm = new ConfirmRemoveMemberDialog();
		confirm.removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				teamMembersPanel.remove(view);
				presenter.removeTeamMember(teamMember);
				confirm.hide();
			}
		});
		confirm.center();
	}

	private void updateTeamMember(ProjectTeamMember teamMember) {
		presenter.updateTeamMember(teamMember);
	}

	@UiHandler("inviteButton")
	void onInviteUser(ClickEvent event) {
		presenter.sendInvite(inviteEmail.getText());
	}

}
