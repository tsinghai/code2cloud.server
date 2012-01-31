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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.People;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.presenter.person.PersonUtil;
import com.tasktop.c2c.server.profile.web.shared.ProjectRole;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.TeamMemberView;
import com.tasktop.c2c.server.tasks.client.widgets.WidgetUtil;

public class TeamMemberPresenter extends AbstractProfilePresenter implements ClickHandler {

	private TeamMemberView view;
	private String projectIdentifier;
	private final ProjectTeamMember member;
	private boolean self;
	private final boolean enableEditingAsOwner;
	private final ProjectTeamPresenter teamPresenter;

	public TeamMemberPresenter(ProjectTeamPresenter teamPresenter, TeamMemberView view, ProjectTeamMember member,
			String projectIdentifier, boolean enableEditingAsOwner) {
		super(view);
		this.teamPresenter = teamPresenter;
		this.view = view;
		this.member = member;
		this.projectIdentifier = projectIdentifier;
		this.enableEditingAsOwner = enableEditingAsOwner;
	}

	@Override
	protected void bind() {
		Person person = PersonUtil.toPerson(member.getProfile());
		self = person.equals(getAppState().getSelf());
		if (self) {
			view.icon.setResource(People.peopleResources.personSelfIcon());
		} else {
			view.icon.setResource(People.peopleResources.personIcon());
		}
		view.name.setText(person.getName());
		view.username.setText(person.getIdentity());

		updateRolesText();

		if (enableEditingAsOwner) {
			view.rolesAnchor.addClickHandler(this);
			if (!self) {
				view.removeButton.setResource(People.peopleResources.removeIcon());
				view.removeButton.setVisible(true);
				view.removeButton.setStyleName("button");
				view.removeButton.addClickHandler(this);
			}
		} else {
			view.rolesAnchor.addStyleName("noclick");
		}
	}

	protected void updateRolesText() {
		String rolesText = "";
		for (ProjectRole role : member.getRoles()) {
			if (rolesText.length() > 0) {
				rolesText += ", ";
			}
			rolesText += role.getLabel();
		}

		view.rolesAnchor.setText(rolesText);
	}

	@Override
	public void show(AcceptsOneWidget container) {
		container.setWidget(view);
	}

	@Override
	public void onClick(ClickEvent event) {
		Object source = event.getSource();
		if (source == view.rolesAnchor) {
			PopupPanel panel = WidgetUtil.createPopupPanel(view.rolesAnchor);
			panel.addAutoHidePartner(view.rolesAnchor.getElement());
			VerticalPanel content = new VerticalPanel();
			for (final ProjectRole role : ProjectRole.values()) {
				CheckBox checkbox = new CheckBox(role.getLabel());
				checkbox.setValue(member.getRoles().contains(role));
				boolean enableChanges = true;
				if (role.equals(ProjectRole.MEMBER) || (role.equals(ProjectRole.OWNER) && self)) {
					enableChanges = false;
				}

				checkbox.setEnabled(enableChanges);
				if (enableChanges) {
					checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue()) {
								member.getRoles().add(role);
							} else {
								member.getRoles().remove(role);
							}
							updateRoles();
						}
					});
				}
				content.add(checkbox);
			}
			panel.add(content);
			if (panel.isShowing()) {
				panel.showRelativeTo(view.rolesAnchor);
			} else {
				panel.hide();
			}
		} else if (source == view.removeButton) {
			removeMember();
		}
	}

	private void removeMember() {

	}

	protected void updateRoles() {

	}
}
