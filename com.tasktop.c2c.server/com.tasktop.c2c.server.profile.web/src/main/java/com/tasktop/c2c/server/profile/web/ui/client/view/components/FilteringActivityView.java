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

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.profile.domain.activity.WikiActivity;

public class FilteringActivityView extends BaseActivityView {
	interface Binder extends UiBinder<Widget, FilteringActivityView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	CheckBox buildToggleButton;
	@UiField
	CheckBox scmToggleButton;
	@UiField
	CheckBox taskToggleButton;
	@UiField
	CheckBox wikiToggleButton;

	public FilteringActivityView() {
		initWidget(uiBinder.createAndBindUi(this));
		enableAllTypes();
		ClickHandler renderHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				render();
			}
		};

		buildToggleButton.addClickHandler(renderHandler);
		scmToggleButton.addClickHandler(renderHandler);
		taskToggleButton.addClickHandler(renderHandler);
		wikiToggleButton.addClickHandler(renderHandler);
	}

	private void enableAllTypes() {
		buildToggleButton.setValue(true);
		scmToggleButton.setValue(true);
		taskToggleButton.setValue(true);
		wikiToggleButton.setValue(true);

	}

	@Override
	protected List<ProjectActivity> filterActivity(List<ProjectActivity> value) {
		List<ProjectActivity> result = new ArrayList<ProjectActivity>(value.size());

		boolean showTask = taskToggleButton.getValue();
		boolean showScm = scmToggleButton.getValue();
		boolean showBuild = buildToggleButton.getValue();
		boolean showWiki = wikiToggleButton.getValue();

		for (ProjectActivity activity : value) {
			if (activity instanceof TaskActivity && !showTask) {
				continue;
			} else if (activity instanceof ScmActivity && !showScm) {
				continue;
			} else if (activity instanceof BuildActivity && !showBuild) {
				continue;
			} else if (activity instanceof WikiActivity && !showWiki) {
				continue;
			}
			result.add(activity);
		}
		return result;
	}

	@Override
	public void renderActivity(List<ProjectActivity> activities) {
		super.renderActivity(activities);

		boolean haveTask = false;
		boolean haveScm = false;
		boolean haveBuild = false;
		boolean haveWiki = false;
		for (ProjectActivity activity : activities) {
			if (activity instanceof TaskActivity) {
				haveTask = true;
			} else if (activity instanceof ScmActivity) {
				haveScm = true;
			} else if (activity instanceof BuildActivity) {
				haveBuild = true;
			} else if (activity instanceof WikiActivity) {
				haveWiki = true;
			}
			if (haveTask && haveScm && haveBuild && haveWiki) {
				break;
			}
		}
		taskToggleButton.setVisible(haveTask);
		scmToggleButton.setVisible(haveScm);
		buildToggleButton.setVisible(haveBuild);
		wikiToggleButton.setVisible(haveWiki);
	}

}
