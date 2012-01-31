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
package com.tasktop.c2c.server.profile.web.ui.client.widgets.chooser.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;


public class PersonTest {
	@Test
	public void testEquals() {
		Person person = new Person("One", "two");
		Person person2 = new Person("One", "two");
		assertEquals(person, person2);
		assertEquals(person.hashCode(), person2.hashCode());

		person2.setIdentity(person2.getIdentity() + "x");
		assertFalse(person.equals(person2));
	}
}
