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
package com.tasktop.c2c.server.profile.domain;

import junit.framework.Assert;

import org.junit.Test;

import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;

public class ProjectArtifactTest {

	@Test
	public void testEquals() {
		Assert.assertFalse(makeArtifact(null).equals(makeArtifact(null)));
		ProjectArtifact p1 = makeArtifact(null);
		Assert.assertTrue(p1.equals(p1));
		Assert.assertTrue(makeArtifact(1l).equals(makeArtifact(1l)));

	}

	private ProjectArtifact makeArtifact(Long id) {
		return makeArtifact("name", id);
	}

	private ProjectArtifact makeArtifact(String name, Long id) {
		ProjectArtifact result = new ProjectArtifact();
		result.setName(name);
		result.setId(id);
		return result;
	}
}
