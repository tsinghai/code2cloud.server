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
import java.math.MathContext;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodUnit {
	public static final TimePeriodUnit MINUTES = new TimePeriodUnit(BigDecimal.ONE, "m");
	public static final TimePeriodUnit HOURS = new TimePeriodUnit(new BigDecimal(60), "h");
	public static final TimePeriodUnit DAYS = new TimePeriodUnit(new BigDecimal(60 * 8), "d"); // Currently hardcoded 1d
																								// = 8h.

	private final String decimalPattern = "(\\d*\\.?\\d*)\\s*";

	private String unitString;
	private RegExp partPattern;
	/** Number of minutes in this period. */
	private BigDecimal minuteMultiplyer;

	public TimePeriodUnit(BigDecimal minuteMultiplyer, String unitPattern) {
		this.minuteMultiplyer = minuteMultiplyer;
		this.partPattern = RegExp.compile(decimalPattern + unitPattern);
		this.unitString = unitPattern;
	}

	public BigDecimal convertToMinutes(BigDecimal valueInThisPeriod) {
		return valueInThisPeriod.multiply(minuteMultiplyer);
	}

	public BigDecimal convertFromMinutes(BigDecimal valueInMinutes) {
		return valueInMinutes.divide(minuteMultiplyer, MathContext.DECIMAL32);
	}

	public BigDecimal convert(BigDecimal valueInThisPeriod, TimePeriodUnit otherPeriod) {
		BigDecimal timeInMin = convertToMinutes(valueInThisPeriod);
		return otherPeriod.convertFromMinutes(timeInMin);
	}

	public BigDecimal parsePart(String periodString) {
		MatchResult match = partPattern.exec(periodString);
		if (match == null) {
			return null;
		}
		String decimal = match.getGroup(1);
		return new BigDecimal(decimal);
	}

	/**
	 * @return the unitString
	 */
	public String getUnitString() {
		return unitString;
	}

}
