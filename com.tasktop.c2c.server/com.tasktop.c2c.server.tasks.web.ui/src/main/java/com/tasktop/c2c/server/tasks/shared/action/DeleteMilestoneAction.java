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

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class DeleteMilestoneAction implements Action<DeleteMilestoneResult>, WriteAction {

	private String projectId;
	private Integer milestoneId;

	public DeleteMilestoneAction(String projectId, Integer milestoneId) {
		this.projectId = projectId;
		this.milestoneId = milestoneId;
	}

	protected DeleteMilestoneAction() {

	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @return the productId
	 */
	public Integer getMilestoneId() {
		return milestoneId;
	}

}
