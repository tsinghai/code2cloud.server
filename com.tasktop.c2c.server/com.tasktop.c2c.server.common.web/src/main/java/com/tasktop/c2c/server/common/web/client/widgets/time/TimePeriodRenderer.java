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
import java.math.RoundingMode;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodRenderer extends AbstractRenderer<BigDecimal> {

	public static TimePeriodRenderer HOUR_RENDERER = new TimePeriodRenderer(TimePeriodUnit.HOURS, TimePeriodUnit.DAYS,
			TimePeriodUnit.HOURS, TimePeriodUnit.MINUTES);

	private TimePeriodUnit valueUnit;
	private TimePeriodUnit[] unitsFromHighToLow;

	private TimePeriodRenderer(TimePeriodUnit valueUnit, TimePeriodUnit... unitsFromHighToLow) {
		this.valueUnit = valueUnit;
		this.unitsFromHighToLow = unitsFromHighToLow;
	}

	@Override
	public String render(BigDecimal value) {
		if (value == null) {
			return "";
		}
		if (value.signum() == 0) {
			return "0" + valueUnit.getUnitString();
		}
		BigDecimal valueInMin = valueUnit.convertToMinutes(value);
		String result = "";
		for (TimePeriodUnit tpu : unitsFromHighToLow) {
			BigDecimal valueInUnit = tpu.convertFromMinutes(valueInMin);
			BigDecimal wholePartInUnit = valueInUnit.setScale(0, RoundingMode.FLOOR);
			if (wholePartInUnit.signum() == 0) {
				continue;
			}
			if (!result.isEmpty()) {
				result = result + " ";
			}
			result = result + format(wholePartInUnit) + tpu.getUnitString();
			BigDecimal wholePartInMinutes = tpu.convertToMinutes(wholePartInUnit);
			valueInMin = valueInMin.subtract(wholePartInMinutes);
		}

		return result;
	}

	private String format(BigDecimal value) {
		return value.toPlainString();
	}

}
