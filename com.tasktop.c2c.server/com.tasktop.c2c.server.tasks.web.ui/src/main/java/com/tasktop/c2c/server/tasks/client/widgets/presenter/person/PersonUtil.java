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
package com.tasktop.c2c.server.tasks.client.widgets.presenter.person;

import java.util.ArrayList;
import java.util.List;


import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public class PersonUtil {

	public static List<Person> toPeople(List<TaskUserProfile> userProfiles) {
		if (userProfiles == null) {
			return new ArrayList<Person>();
		}
		List<Person> people = new ArrayList<Person>(userProfiles.size());
		for (TaskUserProfile userProfile : userProfiles) {
			people.add(toPerson(userProfile));
		}
		return people;
	}

	public static Person toPerson(TaskUserProfile userProfile) {
		if (userProfile == null) {
			return null;
		}
		return new Person(userProfile.getLoginName(), userProfile.getRealname(), userProfile.getId());
	}

	public static List<TaskUserProfile> toTaskUserProfiles(List<Person> persons) {
		List<TaskUserProfile> taskUserProfiles = new ArrayList<TaskUserProfile>();
		for (Person person : persons) {
			taskUserProfiles.add(toTaskUserProfile(person));
		}
		return taskUserProfiles;
	}

	public static TaskUserProfile toTaskUserProfile(Person person) {
		if (person == null) {
			return null;
		}
		TaskUserProfile taskUserProfile = new TaskUserProfile();
		taskUserProfile.setLoginName(person.getIdentity());
		taskUserProfile.setRealname(person.getName());
		taskUserProfile.setId(person.getId());
		return taskUserProfile;
	}

}
