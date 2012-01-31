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
import com.tasktop.c2c.server.wiki.web.ui.shared.action.DeletePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.DeletePageResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class DeletePageActionHandler extends AbstractWikiActionHandler<DeletePageAction, DeletePageResult> {

	@Override
	public DeletePageResult execute(DeletePageAction action, ExecutionContext context) throws DispatchException {
		try {
			getService(action.getProjectId()).deletePage(action.getPageId());
			return new DeletePageResult();
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

}
