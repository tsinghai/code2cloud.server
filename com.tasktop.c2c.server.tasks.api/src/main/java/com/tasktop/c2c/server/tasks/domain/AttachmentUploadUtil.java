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
package com.tasktop.c2c.server.tasks.domain;

/**
 * Shared class for working with file attachment form uploads.
 */
public final class AttachmentUploadUtil {
	private AttachmentUploadUtil() {
		// Static helper class
	}

	public static final String TASK_HANDLE_FORM_NAME = "TaskHandle";

	/** Should be one per attachment, with index appended (zero-based). */
	public static final String ATTACHMENT_DESCRIPTION_FORM_NAME_PREFIX = "description";

	// RESULT CODES

	public static final String CONCURRENT_UPDATE_ERROR = "ConcurrentUpdate";
	public static final String VALIDATION_ERROR = "ValidationError";
	public static final String ENTITY_NOT_FOUND_ERROR = "EntityNotFound";

	public static String createTaskHandleValue(TaskHandle taskHandle) {
		return taskHandle.getId() + "-" + taskHandle.getVersion();
	}

	public static TaskHandle parseTaskHandleValue(String value) {
		TaskHandle result = new TaskHandle();
		String[] splits = value.split("-");
		if (splits.length != 2) {
			throw new IllegalArgumentException("Can not parse task handle value: " + value);
		}
		result.setId(Integer.parseInt(splits[0]));
		result.setVersion(splits[1]);
		return result;
	}
}
