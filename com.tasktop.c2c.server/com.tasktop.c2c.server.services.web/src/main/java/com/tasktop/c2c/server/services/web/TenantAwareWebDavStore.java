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
package com.tasktop.c2c.server.services.web;

import java.io.File;
import java.io.InputStream;

import net.sf.webdav.ITransaction;
import net.sf.webdav.LocalFileSystemStore;
import net.sf.webdav.StoredObject;

import org.springframework.security.access.annotation.Secured;
import org.springframework.tenancy.context.TenancyContextHolder;

import com.tasktop.c2c.server.common.service.domain.Role;

public class TenantAwareWebDavStore extends LocalFileSystemStore {

	public TenantAwareWebDavStore(File root) {
		super(root);
	}

	private String prepareApplicationPath(String uri) {

		// Task 1122 - strip out relative paths to prevent directory traversal attacks
		String retUri = uri.replaceAll("/\\.\\./", "/").replaceAll("/\\.\\.", "/");

		return TenancyContextHolder.getContext().getTenant().getIdentity() + "/" + retUri;
	}

	@Override
	@Secured(Role.User)
	public void createFolder(ITransaction transaction, String folderUri) {
		super.createFolder(transaction, prepareApplicationPath(folderUri));
	}

	@Override
	@Secured(Role.User)
	public void createResource(ITransaction transaction, String resourceUri) {
		super.createResource(transaction, prepareApplicationPath(resourceUri));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public InputStream getResourceContent(ITransaction transaction, String resourceUri) {
		return super.getResourceContent(transaction, prepareApplicationPath(resourceUri));
	}

	@Override
	@Secured(Role.User)
	public long setResourceContent(ITransaction transaction, String resourceUri, InputStream content,
			String contentType, String characterEncoding) {
		return super.setResourceContent(transaction, prepareApplicationPath(resourceUri), content, contentType,
				characterEncoding);
	}

	@Override
	@Secured(Role.User)
	public void removeObject(ITransaction transaction, String uri) {
		super.removeObject(transaction, prepareApplicationPath(uri));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public String[] getChildrenNames(ITransaction transaction, String folderUri) {
		return super.getChildrenNames(transaction, prepareApplicationPath(folderUri));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public long getResourceLength(ITransaction transaction, String path) {
		return super.getResourceLength(transaction, prepareApplicationPath(path));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public StoredObject getStoredObject(ITransaction transaction, String uri) {
		return super.getStoredObject(transaction, prepareApplicationPath(uri));
	}
}
