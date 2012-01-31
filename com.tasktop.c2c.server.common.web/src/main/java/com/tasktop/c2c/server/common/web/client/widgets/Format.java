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
package com.tasktop.c2c.server.common.web.client.widgets;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class Format {
	public static String stringValue(Object value) {
		return value == null ? "" : value.toString();
	}

	public static String stringValueDate(Date value) {
		if (!GWT.isClient()) {
			return value == null ? "" : value.toGMTString();
		}
		DateTimeFormat dateTimeFormat = getDateFormat();
		return value == null ? "" : dateTimeFormat.format(value);
	}

	public static DateTimeFormat getDateFormat() {
		return DateTimeFormat.getFormat("MMM d, yyyy");
	}

	public static String stringValueDateTime(Date value) {
		if (!GWT.isClient()) {
			return value == null ? "" : value.toGMTString();
		}
		DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMM d, yyyy h:mm a");
		return value == null ? "" : dateTimeFormat.format(value);
	}

	public static String stringValueTime(Date value) {
		if (!GWT.isClient()) {
			return value == null ? "" : value.toGMTString();
		}
		DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("h:mm a");
		return value == null ? "" : dateTimeFormat.format(value);
	}

	public static String stringValue(DateTimeFormat format, Date value) {
		return value == null ? "" : format.format(value);
	}
}
