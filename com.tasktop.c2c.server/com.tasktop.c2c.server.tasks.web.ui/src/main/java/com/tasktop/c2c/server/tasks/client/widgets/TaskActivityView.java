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
package com.tasktop.c2c.server.tasks.client.widgets;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonLabel;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskActivityView extends Composite {

	interface Binder extends UiBinder<Widget, TaskActivityView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	HTMLPanel actionWrapper;
	@UiField
	PersonLabel personLabel;
	@UiField
	Label changeType;
	@UiField
	Label dateLabel;
	@UiField
	HasText descriptionLabel;
	@UiField
	DivElement timeDiv;
	@UiField
	Label timeLabel;

	public TaskActivityView(TaskActivity taskActivity) {
		initWidget(uiBinder.createAndBindUi(this));

		Person person = new Person(taskActivity.getAuthor().getLoginName(), taskActivity.getAuthor().getRealname());
		Person self = ProfileGinjector.get.instance().getAppState().getSelf();

		personLabel.setAsSelf(self != null && person.getIdentity().equals(self.getIdentity()));
		personLabel.setPerson(person);
		dateLabel.setText(Format.stringValueDateTime(taskActivity.getActivityDate()));
		changeType.setText(taskActivity.getActivityType().getPrettyName());

		descriptionLabel.setText(taskActivity.getDescription());
		setStyleFromActivityType(taskActivity.getActivityType());

		if (taskActivity.getActivityType().equals(
				com.tasktop.c2c.server.tasks.domain.TaskActivity.Type.LOGGED_TIME)) {
			UIObject.setVisible(timeDiv, true);
			timeLabel.setText(TimePeriodRenderer.HOUR_RENDERER.render(taskActivity.getWorkLog().getHoursWorked()));
		} else {
			UIObject.setVisible(timeDiv, false);
		}

	}

	// FIXME UGLY STYLING CONSTANTS
	private void setStyleFromActivityType(Type activityType) {
		switch (activityType) {
		case COMMENTED:
		case LOGGED_TIME:
			actionWrapper.addStyleName("comments");
			return;
		case CREATED:
			actionWrapper.addStyleName("tasks-created");
			return;
		case ATTACHED:
			actionWrapper.addStyleName("tasks-attached");
			return;
		case UPDATED:
		default:
			actionWrapper.addStyleName("tasks");
			return;
		}
	}

}
