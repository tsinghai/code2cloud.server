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
package com.tasktop.c2c.server.common.service;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CsvWriter {
	private String seperator;
	private StringBuilder buffer = new StringBuilder();
	boolean needSeperator = false;
	private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss z");

	public CsvWriter() {
		this(",");
	}

	public CsvWriter(String seperator) {
		this.seperator = seperator;
	}

	public CsvWriter row() {
		buffer.append("\n");
		needSeperator = false;
		return this;
	}

	public CsvWriter value(Object value) {
		String valueString;
		if (value == null) {
			valueString = "";
		} else if (value instanceof Date) {
			valueString = dateFormat.format((Date) value);
		} else {
			valueString = value.toString();
		}
		addValueString(valueString);
		return this;
	}

	private void addValueString(String value) {
		if (needSeperator) {
			buffer.append(seperator);
		} else {
			needSeperator = true;
		}

		value = value.replace("\"", "\"\"");
		buffer.append("\"").append(value).append("\"");
	}

	public CsvWriter write(Writer writer) throws IOException {
		writer.write(buffer.toString());
		buffer = new StringBuilder();
		needSeperator = false;
		return this;
	}
}
