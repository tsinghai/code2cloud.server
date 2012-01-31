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
package com.tasktop.c2c.server.tasks.tests.domain.mock;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.tasks.domain.Attachment;
import com.tasktop.c2c.server.internal.tasks.domain.AttachmentData;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Task;


public class MockAttachmentFactory {

	public static Attachment create(EntityManager entityManager, Task task, Profile profile) {
		Attachment attachement = new Attachment();
		attachement.setFilename("filename");
		attachement.setBugs(task);
		attachement.setDescription("description");
		attachement.setMimetype("mime/Type");
		attachement.setIsobsolete(false);
		attachement.setIspatch(Byte.MIN_VALUE);
		attachement.setIsprivate(false);
		attachement.setIsurl(false);
		attachement.setProfiles(profile);

		if (entityManager != null) {
			entityManager.persist(attachement);
			entityManager.flush();
		}

		AttachmentData data = new AttachmentData();
		data.setAttachment(attachement);
		attachement.setAttachData(data);
		data.setThedata("DATA".getBytes());

		if (entityManager != null) {
			entityManager.persist(data);
		}

		return attachement;
	}

}
