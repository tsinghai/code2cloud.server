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

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.internal.tasks.domain.Fielddef;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;


@Component
public class TaskActivityConverter implements ObjectConverter<TaskActivity> {

	private static final String CUSTOM_FIELD_PREFIX = "cf_";

	@Override
	public boolean supportsSource(Class<?> sourceClass) {
		return com.tasktop.c2c.server.internal.tasks.domain.TaskActivity.class.isAssignableFrom(sourceClass);
	}

	@Override
	public Class<TaskActivity> getTargetClass() {
		return TaskActivity.class;
	}

	@Override
	public void copy(TaskActivity targetObject, Object sourceObject, DomainConverter converter,
			DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.TaskActivity source = (com.tasktop.c2c.server.internal.tasks.domain.TaskActivity) sourceObject;

		targetObject.setActivityType(Type.UPDATED);
		targetObject.setAuthor((TaskUserProfile) converter.convert(source.getProfiles(), context));

		FieldUpdate fieldUpdate = new FieldUpdate();
		Fielddef fielddef = source.getFielddefs();
		if (fielddef.getName().endsWith("longdesc")) {
			fieldUpdate.setFieldDescription("Description"); // Special case for first comment
		} else {
			fieldUpdate.setFieldDescription(fielddef.getDescription());
		}
		if (fielddef.getCustom() && fielddef.getName().startsWith(CUSTOM_FIELD_PREFIX)) {
			fieldUpdate.setFieldName(fielddef.getName().substring(CUSTOM_FIELD_PREFIX.length()));
		} else {
			fieldUpdate.setFieldName(fielddef.getName());
		}
		fieldUpdate.setNewValue(source.getId().getAdded());
		fieldUpdate.setOldValue(source.getId().getRemoved());
		targetObject.setFieldUpdates(Arrays.asList(fieldUpdate));

		targetObject.setTask((Task) converter.convert(source.getBugs(), context));
		targetObject.setActivityDate(source.getId().getBugWhen());
	}

}
