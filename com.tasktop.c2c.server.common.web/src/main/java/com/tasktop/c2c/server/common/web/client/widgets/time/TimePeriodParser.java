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
import java.text.ParseException;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodParser implements Parser<BigDecimal> {

	/** Parses strings and returns values in hours. If no units provided assumes units are hours. */
	public static TimePeriodParser DEFAULT_HOUR_IN_HOURS = new TimePeriodParser(TimePeriodUnit.HOURS,
			TimePeriodUnit.HOURS, TimePeriodUnit.DAYS, TimePeriodUnit.HOURS, TimePeriodUnit.MINUTES);
	/** Parses strings and returns values in hours. If no units provided assumes units are days. */
	public static TimePeriodParser DEFAULT_DAY_IN_HOURS = new TimePeriodParser(TimePeriodUnit.HOURS,
			TimePeriodUnit.DAYS, TimePeriodUnit.DAYS, TimePeriodUnit.HOURS, TimePeriodUnit.MINUTES);

	private TimePeriodUnit defaultUnit;
	private TimePeriodUnit returnUnit;
	private TimePeriodUnit[] supportedUnits;

	private static final RegExp DEFAULT_PATTERN = RegExp.compile("^(\\d*\\.?\\d*)$");

	private TimePeriodParser(TimePeriodUnit returnUnit, TimePeriodUnit defaultUnit, TimePeriodUnit... supportedUnits) {
		this.returnUnit = returnUnit;
		this.defaultUnit = defaultUnit;
		this.supportedUnits = supportedUnits;
	}

	@Override
	public BigDecimal parse(CharSequence text) throws ParseException {
		String string = text.toString().trim();
		if (string.isEmpty()) {
			return null;
		}
		try {

			MatchResult defaultMatch = DEFAULT_PATTERN.exec(string);
			if (defaultMatch != null) {
				return defaultUnit.convert(new BigDecimal(string), returnUnit);
			}

			BigDecimal resultInMin = null;

			for (TimePeriodUnit tpu : supportedUnits) {
				BigDecimal amount = tpu.parsePart(string);
				if (amount != null) {
					BigDecimal amountInMin = tpu.convertToMinutes(amount);
					if (resultInMin == null) {
						resultInMin = amountInMin;
					} else {
						resultInMin = resultInMin.add(amountInMin);
					}
				}
			}

			if (resultInMin == null) {
				return null;
			}
			return returnUnit.convertFromMinutes(resultInMin);

		} catch (NumberFormatException e) {
			return null;
		}
	}

}
