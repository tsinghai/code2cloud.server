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
package com.tasktop.c2c.server.profile.web.client.presenter.person;

import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class PersonUtil {
	public static Person toPerson(com.tasktop.c2c.server.profile.web.shared.Profile profile) {
		if (profile == null) {
			return null;
		}
		return new Person(profile.getUsername(), profile.getFirstName() + ' ' + profile.getLastName());
	}
}
