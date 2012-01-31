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
package com.tasktop.c2c.server.internal.wiki.server;

import org.springframework.tenancy.datasource.TenantAwareDataSource;

public class WikiTenantAwareDataSource extends TenantAwareDataSource {

	private Language language;

	@Override
	protected String getDatabaseName() {
		if (language == Language.HSQL) {
			return super.getDatabaseName().toUpperCase() + "_WIKI";
		}
		return super.getDatabaseName() + "_wiki";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.tenancy.datasource.AbstractDatabaseSwitchingDataSource#setLanguage(org.springframework.tenancy
	 * .datasource.AbstractDatabaseSwitchingDataSource.Language)
	 */
	@Override
	public void setLanguage(Language l) {
		super.setLanguage(l);
		this.language = l;
	}

}
