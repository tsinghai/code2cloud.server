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
package com.tasktop.c2c.server.tasks.tests.util;

import org.junit.Ignore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.tenancy.context.TenancyContext;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.context.TenancyContextHolderStrategy;
import org.springframework.tenancy.context.DefaultTenancyContext;
import org.springframework.tenancy.provider.DefaultTenant;


@Ignore
public class TestTenancyContextInitializer implements InitializingBean {

	private String tenantIdentity;

	@Override
	public void afterPropertiesSet() throws Exception {
		final DefaultTenancyContext tc = new DefaultTenancyContext();
		final DefaultTenant t = new DefaultTenant();
		t.setIdentity(tenantIdentity);
		tc.setTenant(t);
		TenancyContextHolder.setStrategy(new TenancyContextHolderStrategy() {

			@Override
			public void setContext(TenancyContext context) {
				//

			}

			@Override
			public TenancyContext getContext() {
				return tc;
			}

			@Override
			public TenancyContext createEmptyContext() {
				return tc;
			}

			@Override
			public void clearContext() {

			}
		});

	}

	public String getTenantIdentity() {
		return tenantIdentity;
	}

	public void setTenantIdentity(String tenantIdentity) {
		this.tenantIdentity = tenantIdentity;
	}

}
