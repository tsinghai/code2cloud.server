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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public abstract class BaseActivityView extends Composite {

	@UiField
	Panel activityPanel;

	protected Map<String, List<ProjectActivity>> activityByDay;

	protected void render() {
		// Clear our previous data.
		clear();

		if (activityByDay == null) {
			return;
		}

		for (Entry<String, List<ProjectActivity>> entry : activityByDay.entrySet()) {
			List<ProjectActivity> activityForDay = filterActivity(entry.getValue());
			if (activityForDay.isEmpty()) {
				continue;
			}

			ActivityDay activityDay = new ActivityDay();
			activityDay.renderActivityDay(entry.getKey(), activityForDay);
			activityPanel.add(activityDay);

		}

	}

	/**
	 * @param value
	 * @return
	 */
	protected List<ProjectActivity> filterActivity(List<ProjectActivity> value) {
		return value;
	}

	/**
	 * 
	 */
	public BaseActivityView() {
		super();
	}

	public void clear() {
		activityPanel.clear();
	}

	public void renderActivity(List<ProjectActivity> activities) {
		Map<String, List<ProjectActivity>> activityByDate = new LinkedHashMap<String, List<ProjectActivity>>();

		String currentDate = null;
		List<ProjectActivity> activityForDate = new ArrayList<ProjectActivity>();
		for (ProjectActivity activity : activities) {
			String thisActivityDay = getCurrentDay(activity.getActivityDate());
			if (currentDate == null) {
				currentDate = thisActivityDay;
			}
			if (!currentDate.equals(thisActivityDay)) {
				activityByDate.put(currentDate, activityForDate);
				activityForDate = new ArrayList<ProjectActivity>();
				currentDate = thisActivityDay;
			}
			activityForDate.add(activity);
		}
		if (currentDate != null) {
			activityByDate.put(currentDate, activityForDate);
		}
		renderActivity(activityByDate);
	}

	private String getCurrentDay(Date date) {
		return Format.stringValueDate(date);
	}

	private void renderActivity(Map<String, List<ProjectActivity>> activityByDay) {
		this.activityByDay = activityByDay;
		render();

	}

}
