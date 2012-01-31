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

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodParser;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodParserTest {

	@Test
	public void testDefaultHour() throws ParseException {
		// testParse("1", TimePeriodParser.DEFAULT_HOUR, "1");
		// testParse("60m", TimePeriodParser.DEFAULT_HOUR, "1");
		// testParse("1h60m", TimePeriodParser.DEFAULT_HOUR, "2");
		testParse("1h30m", TimePeriodParser.DEFAULT_HOUR_IN_HOURS, "1.5");
		testParse("30m", TimePeriodParser.DEFAULT_HOUR_IN_HOURS, ".5");
		testParse("1d30m", TimePeriodParser.DEFAULT_HOUR_IN_HOURS, "8.5");
	}

	@Test
	public void testRender() {
		testRender("6.5", TimePeriodRenderer.HOUR_RENDERER, "6h 30m");
		testRender(".5", TimePeriodRenderer.HOUR_RENDERER, "30m");
		testRender("8", TimePeriodRenderer.HOUR_RENDERER, "1d");
	}

	private void testParse(String string, TimePeriodParser parser, String expectedBDString) throws ParseException {
		BigDecimal result = parser.parse(string);
		if (expectedBDString == null) {
			Assert.assertNull(result);
		} else {
			Assert.assertNotNull(result);
			BigDecimal expected = new BigDecimal(expectedBDString);
			Assert.assertTrue(expected.toString() + " != " + result.toString(), expected.compareTo(result) == 0);
		}
	}

	private void testRender(String bdStringToRender, TimePeriodRenderer renderer, String expectedResult) {
		String result = renderer.render(new BigDecimal(bdStringToRender));
		Assert.assertEquals(expectedResult, result);
	}
}
