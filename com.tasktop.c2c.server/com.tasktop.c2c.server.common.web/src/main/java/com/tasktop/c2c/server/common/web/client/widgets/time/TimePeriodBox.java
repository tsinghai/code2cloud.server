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
package com.tasktop.c2c.server.common.web.client.widgets.time;

import java.math.BigDecimal;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodBox extends ValueBox<BigDecimal> {

	public static TimePeriodBox getDefaultHourBox() {
		return new TimePeriodBox(TimePeriodRenderer.HOUR_RENDERER, TimePeriodParser.DEFAULT_HOUR_IN_HOURS);
	}

	public static TimePeriodBox getHourBoxWithDefaultsEntryToDays() {
		return new TimePeriodBox(TimePeriodRenderer.HOUR_RENDERER, TimePeriodParser.DEFAULT_DAY_IN_HOURS);
	}

	/**
	 * @param element
	 * @param renderer
	 * @param parser
	 */
	public TimePeriodBox(TimePeriodRenderer renderer, TimePeriodParser parser) {
		super(Document.get().createTextInputElement(), renderer, parser);
	}
}
