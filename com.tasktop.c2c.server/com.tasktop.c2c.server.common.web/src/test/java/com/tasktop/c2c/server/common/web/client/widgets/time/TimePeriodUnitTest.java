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

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodUnit;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TimePeriodUnitTest {

	@Test
	public void testMinute() {
		testParse("foobar", TimePeriodUnit.MINUTES, null);
		testParse("1m", TimePeriodUnit.MINUTES, "1");
		testParse("1.0m", TimePeriodUnit.MINUTES, "1");
		testParse("1.0   m", TimePeriodUnit.MINUTES, "1");
		testParse("001.000   m", TimePeriodUnit.MINUTES, "1");
		testParse("123.000   m", TimePeriodUnit.MINUTES, "123");
		testParse("8h 365d 13.000   m", TimePeriodUnit.MINUTES, "13");
		testParse("13 m 8h 365d", TimePeriodUnit.MINUTES, "13");

	}

	private void testParse(String string, TimePeriodUnit unit, String expectedBDString) {
		BigDecimal result = unit.parsePart(string);
		if (expectedBDString == null) {
			Assert.assertNull(result);
		} else {
			Assert.assertNotNull(result);
			BigDecimal expected = new BigDecimal(expectedBDString);
			Assert.assertTrue(expected.compareTo(result) == 0);
		}

	}
}
