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

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A task resolution, used when a task is marked as {@link TaskSeverity resolved}
 */
public class TaskResolution extends AbstractReferenceValue {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	public boolean isDuplicate() {
		// FIXME there must be a better way
		return "duplicate".equalsIgnoreCase(getValue());
	}

}
