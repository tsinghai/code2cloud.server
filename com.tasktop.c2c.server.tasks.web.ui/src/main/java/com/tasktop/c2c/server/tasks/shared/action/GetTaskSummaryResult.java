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
package com.tasktop.c2c.server.tasks.shared.action;

import java.io.Serializable;
import java.util.List;

import net.customware.gwt.dispatch.shared.AbstractSimpleResult;

import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class GetTaskSummaryResult extends AbstractSimpleResult<List<Task>> implements Serializable {
	private static final long serialVersionUID = 1L;

	protected GetTaskSummaryResult() {
		super();
	}

	public GetTaskSummaryResult(List<Task> value) {
		super(value);
	}

}
