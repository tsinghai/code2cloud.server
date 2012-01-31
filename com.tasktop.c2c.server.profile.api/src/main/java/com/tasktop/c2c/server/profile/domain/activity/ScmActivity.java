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
package com.tasktop.c2c.server.profile.domain.activity;

import com.tasktop.c2c.server.profile.domain.scm.Commit;

@SuppressWarnings("serial")
public class ScmActivity extends ProjectActivity {
	private Commit commit;

	public ScmActivity(Commit commit) {
		this.commit = commit;
		super.setActivityDate(commit.getDate());
	}

	public ScmActivity() {
		// nothing
	}

	public Commit getCommit() {
		return commit;
	}

	public void setCommit(Commit commit) {
		this.commit = commit;
	}
}
