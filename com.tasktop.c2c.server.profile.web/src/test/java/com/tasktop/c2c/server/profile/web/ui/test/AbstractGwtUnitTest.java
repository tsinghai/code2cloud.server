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
package com.tasktop.c2c.server.profile.web.ui.test;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;

import com.google.gwt.junit.GWTMockUtilities;

public abstract class AbstractGwtUnitTest {

	protected Mockery context = new JUnit4Mockery() {
		{
			// Enable CGLIB mocking of concrete classes
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	public AbstractGwtUnitTest() {
		super();
	}

	@Before
	public void disableWidgets() {
		GWTMockUtilities.disarm();
	}

	@After
	public void reEnableWidgets() {
		GWTMockUtilities.restore();
	}

}
