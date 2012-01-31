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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonLabel;
import com.tasktop.c2c.server.profile.domain.activity.WikiActivity;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;

public class WikiActivityRow extends Composite {
	interface Binder extends UiBinder<Widget, WikiActivityRow> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	public WikiActivityRow(WikiActivity activity) {
		initWidget(uiBinder.createAndBindUi(this));
		render(activity);
	}

	@UiField
	Anchor pageAnchor;
	@UiField
	PersonLabel personLabel;
	@UiField
	Label dateLabel;
	@UiField
	Label changeType;

	private void render(WikiActivity activity) {
		Person person = new Person(activity.getActivity().getAuthor().getLoginName(), activity.getActivity()
				.getAuthor().getName());
		Person self = ProfileEntryPoint.getInstance().getAppState().getSelf();
		personLabel.setAsSelf(self != null && person.getIdentity().equals(self.getIdentity()));
		personLabel.setPerson(person);
		dateLabel.setText(Format.stringValueTime(activity.getActivityDate()));
		changeType.setText(activity.getActivity().getActivityType().getLabel());
		pageAnchor.setText("Page " + activity.getActivity().getPage().getPath());
		pageAnchor.setHref(activity.getActivity().getPage().getUrl());
	}

}
