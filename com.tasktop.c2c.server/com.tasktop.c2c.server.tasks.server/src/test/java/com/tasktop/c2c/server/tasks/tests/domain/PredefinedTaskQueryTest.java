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
package com.tasktop.c2c.server.tasks.tests.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;


public class PredefinedTaskQueryTest {

	@Test
	public void testValueOfString() {
		for (PredefinedTaskQuery query : PredefinedTaskQuery.values()) {
			assertEquals(query, PredefinedTaskQuery.valueOfString(query.toString()));
		}
	}

	@Test
	public void testDefault() {
		assertNotNull(PredefinedTaskQuery.DEFAULT);
	}

}
