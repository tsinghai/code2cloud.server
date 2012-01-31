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
package com.tasktop.c2c.server.common.jpa;

import org.eclipse.persistence.platform.database.MySQLPlatform;

/**
 * see bug 333715: MySQLPlatform.computeMaxRowsForSQL computes incorrect value
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=333715
 * 
 * @author David Green
 */
public class MySQL55Platform extends MySQLPlatform {

	@Override
	public int computeMaxRowsForSQL(int firstResultIndex, int maxResults) {
		// fix bug 333715 in superclass: see http://dev.mysql.com/doc/refman/5.5/en/select.html
		return maxResults;
	}
}
