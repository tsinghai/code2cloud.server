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
import com.tasktop.c2c.server.tasks.domain.Keyword;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class UpdateKeywordAction implements Action<UpdateKeywordResult>, WriteAction {

	private String projectId;
	private Keyword keyword;

	public UpdateKeywordAction(String projectId, Keyword keyword) {
		this.projectId = projectId;
		this.keyword = keyword;
	}

	protected UpdateKeywordAction() {

	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @return the keyword
	 */
	public Keyword getKeyword() {
		return keyword;
	}

}
