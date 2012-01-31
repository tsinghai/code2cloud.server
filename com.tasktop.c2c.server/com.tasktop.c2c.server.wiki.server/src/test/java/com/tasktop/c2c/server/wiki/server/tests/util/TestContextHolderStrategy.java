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
package com.tasktop.c2c.server.wiki.server.tests.util;

import org.junit.Ignore;
import org.springframework.tenancy.context.DefaultTenancyContext;
import org.springframework.tenancy.context.TenancyContext;
import org.springframework.tenancy.context.TenancyContextHolderStrategy;

@Ignore
public class TestContextHolderStrategy implements TenancyContextHolderStrategy {

	private TenancyContext context = createEmptyContext();

	@Override
	public void clearContext() {
		context = createEmptyContext();
	}

	@Override
	public TenancyContext createEmptyContext() {
		return new DefaultTenancyContext();
	}

	@Override
	public TenancyContext getContext() {
		return context;
	}

	@Override
	public void setContext(TenancyContext arg0) {
		context = arg0 == null ? createEmptyContext() : arg0;
	}

}
