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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.common.web.client.widgets.HasExpandingTextPanel;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonLabel;
import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.tasks.client.widgets.TaskHyperlinkDetector;

public class ScmActivityRow extends HasExpandingTextPanel {
	interface Binder extends UiBinder<Widget, ScmActivityRow> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	public ScmActivityRow(ScmActivity scmActivity) {
		initWidget(uiBinder.createAndBindUi(this));
		super.setupWidgets();
		expandingTextLabel.addHyperlinkDetector(new TaskHyperlinkDetector(scmActivity.getProjectIdentifier()));
		renderCommit(scmActivity.getCommit());
	}

	@UiField
	Label commitLabel;
	@UiField
	PersonLabel personLabel;
	@UiField
	Label dateLabel;

	private void renderCommit(Commit commit) {
		commitLabel.setText(commit.getNumber().length() > 6 ? commit.getNumber().substring(0, 7) : commit.getNumber());
		Person person = new Person(commit.getAuthor().getUsername(), commit.getAuthor().toFullName());
		Person self = ProfileEntryPoint.getInstance().getAppState().getSelf();
		personLabel.setAsSelf(self != null && person.getIdentity().equals(self.getIdentity()));
		personLabel.setPerson(person);
		dateLabel.setText(Format.stringValueTime(commit.getDate()));
		setExpandingText(commit.getComment());
	}

}
