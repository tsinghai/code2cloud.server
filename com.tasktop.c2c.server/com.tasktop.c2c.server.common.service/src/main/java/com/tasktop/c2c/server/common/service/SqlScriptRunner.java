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
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author david.green (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.) : reworked into general sql scriptRunner
 * 
 */
public class SqlScriptRunner {

	private Logger log = LoggerFactory.getLogger(SqlScriptRunner.class.getName());

	public void processDDL(Connection connection, Reader scriptReader, boolean ignoreFailures) throws SQLException,
			IOException {
		String ddl = loadDDL(scriptReader);
		Statement statement = connection.createStatement();
		try {
			Pattern statementPattern = Pattern.compile("\\S.*?;", Pattern.DOTALL);
			Matcher matcher = statementPattern.matcher(ddl);
			while (matcher.find()) {
				String sql = matcher.group();
				try {
					log.info(sql);

					statement.execute(sql);
				} catch (SQLException e) {
					if (!ignoreFailures) {
						throw e;
					} else {
						log.info("expected failure: " + e.getMessage());
					}
				}
			}
		} finally {
			statement.close();
		}
	}

	private String loadDDL(Reader ddlIn) throws IOException {
		StringWriter out = new StringWriter();

		try {
			int i;
			while ((i = ddlIn.read()) != -1) {
				out.write(i);
			}
		} finally {
			ddlIn.close();
		}

		return out.toString();
	}
}
