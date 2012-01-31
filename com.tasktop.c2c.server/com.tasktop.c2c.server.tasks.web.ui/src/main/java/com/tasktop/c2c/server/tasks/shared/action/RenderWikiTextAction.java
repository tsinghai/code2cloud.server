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

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class RenderWikiTextAction implements Action<RenderWikiTextResult> {

	private String projectId;
	private String wikiText;

	public RenderWikiTextAction(String projectId, String wikiText) {
		this.wikiText = wikiText;
		this.projectId = projectId;
	}

	protected RenderWikiTextAction() {

	}

	public String getWikiText() {
		return wikiText;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

}
