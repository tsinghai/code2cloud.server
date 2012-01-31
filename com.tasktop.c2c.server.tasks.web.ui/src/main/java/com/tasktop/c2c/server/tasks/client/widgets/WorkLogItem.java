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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class WorkLogItem extends Composite {

	interface Binder extends UiBinder<Widget, WorkLogItem> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public Label date;
	@UiField
	public Label hours;
	@UiField
	public Label comment;
	@UiField
	public Label personName;

	public WorkLogItem(WorkLog workLog) {

		initWidget(uiBinder.createAndBindUi(this));
		date.setText(Format.stringValueDate(workLog.getDateWorked()));
		hours.setText(TimePeriodRenderer.HOUR_RENDERER.render(workLog.getHoursWorked()));
		personName.setText(workLog.getProfile().getRealname());
		comment.setText(workLog.getComment() == null ? "" : workLog.getComment());
	}
}
