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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.internal.tasks.domain.Cc;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

@Component
public class ProfileConverter implements ObjectConverter<TaskUserProfile> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return Profile.class.isAssignableFrom(clazz) || Cc.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(TaskUserProfile target, Object internalObject, DomainConverter converter,
			DomainConversionContext context) {
		if (internalObject instanceof Profile) {
			Profile source = (Profile) internalObject;

			target.setId(source.getId());
			target.setRealname(source.getRealname());
			target.setLoginName(source.getLoginName());
			target.setGravatarHash(source.getGravatarHash());
		} else if (internalObject instanceof Cc) {
			copy(target, ((Cc) internalObject).getProfiles(), converter, context);
		}
	}

	@Override
	public Class<TaskUserProfile> getTargetClass() {
		return TaskUserProfile.class;
	}

	public static TaskUserProfile copy(Profile source) {
		TaskUserProfile target = new TaskUserProfile();
		new ProfileConverter().copy(target, source, null, null);
		return target;
	}
}
