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
package com.tasktop.c2c.server.profile.web.ui.server.action;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.service.WikiService;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class ListAttachementsActionHandler extends
		AbstractWikiActionHandler<ListAttachmentsAction, ListAttachmentsResult> {

	@Override
	public ListAttachmentsResult execute(ListAttachmentsAction action, ExecutionContext context)
			throws DispatchException {
		try {
			if (action.getPageId() != null) {
				WikiService service = getService(action.getProjectId());
				return new ListAttachmentsResult(service.listAttachments(action.getPageId()));
			} else if (action.getPagePath() != null) {
				WikiService service = getService(action.getProjectId());
				Page page = service.retrievePageByPath(action.getPagePath());
				return new ListAttachmentsResult(service.listAttachments(page.getId()));
			}
		} catch (EntityNotFoundException e) {
			handle(e);
		}
		throw new IllegalStateException();
	}

}
