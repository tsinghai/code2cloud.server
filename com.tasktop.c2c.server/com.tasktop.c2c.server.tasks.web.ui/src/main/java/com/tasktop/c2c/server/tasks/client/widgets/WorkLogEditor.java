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
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodBox;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class WorkLogEditor extends Composite implements HasValue<WorkLog> {

	interface Binder extends UiBinder<Widget, WorkLogEditor> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public DateBox date;
	@UiField(provided = true)
	public TimePeriodBox hours = TimePeriodBox.getDefaultHourBox();;
	@UiField
	public TextBox comment;

	private WorkLog workLog;

	public WorkLogEditor() {
		initWidget(uiBinder.createAndBindUi(this));
		date.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<WorkLog> handler) {
		// TODO Auto-generated method stub
		return null;
	}

	public void clear() {
		date.setValue(null);
		hours.setValue(null);
		comment.setValue(null);
	}

	@Override
	public WorkLog getValue() {
		if (workLog == null) {
			workLog = new WorkLog();
		}
		// Set only the fields we collect
		workLog.setComment(comment.getText());
		workLog.setDateWorked(date.getValue());
		workLog.setHoursWorked(hours.getValue());
		// Don't set task - avoid infinite loop in json serialization
		// workLog.setTask(task);
		// don't know self here. Server sets person to the current user
		// workLog.setProfile(getAppState().getSelf());
		return workLog;
	}

	@Override
	public void setValue(WorkLog value) {
		setValue(value, false);
	}

	@Override
	public void setValue(WorkLog value, boolean fireEvents) {
		workLog = value;
	}

}
