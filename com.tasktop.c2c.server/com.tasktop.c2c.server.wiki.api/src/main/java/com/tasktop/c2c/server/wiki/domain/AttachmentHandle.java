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
package com.tasktop.c2c.server.wiki.domain;

@SuppressWarnings("serial")
public class AttachmentHandle extends AbstractDomainObject {
	private PageHandle page;

	public AttachmentHandle() {
	}

	public AttachmentHandle(Integer id, PageHandle pageHandle) {
		super(id);
		setPage(pageHandle);
	}

	public PageHandle getPage() {
		return page;
	}

	public void setPage(PageHandle page) {
		this.page = page;
	}

}
