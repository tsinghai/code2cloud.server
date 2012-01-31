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

import net.customware.gwt.dispatch.shared.Action;


import com.tasktop.c2c.server.common.web.shared.WriteAction;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class UpdateCustomFieldAction implements Action<UpdateCustomFieldResult>, WriteAction {

	private String projectId;
	private FieldDescriptor customField;

	public UpdateCustomFieldAction(String projectId, FieldDescriptor customField) {
		this.projectId = projectId;
		this.customField = customField;
	}

	protected UpdateCustomFieldAction() {

	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @return the customField
	 */
	public FieldDescriptor getCustomField() {
		return customField;
	}

}
