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

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.profile.domain.activity.WikiActivity;
import com.tasktop.c2c.server.profile.web.ui.client.widgets.build.BuildResources;

public class ActivityDay extends Composite {
	interface Binder extends UiBinder<Widget, ActivityDay> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	public static BuildResources buildResources = GWT.create(BuildResources.class);

	public ActivityDay() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Label dayLabel;

	@UiField
	Panel dayPanel;

	public void renderActivityDay(String dayString, List<ProjectActivity> activityList) {
		this.dayLabel.setText(dayString);
		for (int i = 0; i < activityList.size(); i++) {
			ProjectActivity activity = activityList.get(i);
			Composite row;

			if (activity instanceof ScmActivity) {
				row = new ScmActivityRow((ScmActivity) activity);
			} else if (activity instanceof TaskActivity) {
				row = new TaskActivityRow((TaskActivity) activity);
			} else if (activity instanceof BuildActivity) {
				row = new BuildActivityRow((BuildActivity) activity);
			} else if (activity instanceof WikiActivity) {
				row = new WikiActivityRow((WikiActivity) activity);
			} else {
				throw new UnsupportedOperationException("Unknown activity type");
			}

			dayPanel.add(row);
		}

	}

}
