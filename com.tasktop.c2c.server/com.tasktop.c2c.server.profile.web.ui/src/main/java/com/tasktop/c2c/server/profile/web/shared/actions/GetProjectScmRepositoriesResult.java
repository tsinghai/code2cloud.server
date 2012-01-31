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
package com.tasktop.c2c.server.profile.web.shared.actions;

import java.util.List;

import net.customware.gwt.dispatch.shared.AbstractSimpleResult;

import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class GetProjectScmRepositoriesResult extends AbstractSimpleResult<List<ScmRepository>> {

	private String gitRepositoryBaseUrl;

	protected GetProjectScmRepositoriesResult() {
		super();
	}

	public GetProjectScmRepositoriesResult(List<ScmRepository> value, String gitRespositoryBaseUrl) {
		super(value);
		this.gitRepositoryBaseUrl = gitRespositoryBaseUrl;
	}

	/**
	 * @return the gitRepositoryBaseUrl
	 */
	public String getGitRepositoryBaseUrl() {
		return gitRepositoryBaseUrl;
	}

}
