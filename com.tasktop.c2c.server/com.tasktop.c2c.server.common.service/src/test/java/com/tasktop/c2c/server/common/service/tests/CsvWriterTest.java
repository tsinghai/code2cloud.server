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
package com.tasktop.c2c.server.common.service.tests;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

import com.tasktop.c2c.server.common.service.CsvWriter;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CsvWriterTest {

	@Test
	public void simpleTest() throws IOException {
		CsvWriter writer = new CsvWriter();
		writer.value("A").value(2).value("C").row();
		StringWriter output = new StringWriter();
		writer.write(output);
		Assert.assertEquals("\"A\",\"2\",\"C\"\n", output.getBuffer().toString());
	}
}
