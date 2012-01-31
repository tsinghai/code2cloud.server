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
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class RetrievePageActionHandler extends AbstractWikiActionHandler<RetrievePageAction, RetrievePageResult> {

	@Override
	public RetrievePageResult execute(RetrievePageAction action, ExecutionContext context) throws DispatchException {
		try {
			if (action.getPageId() != null) {
				if (action.isRenderContent()) {
					// ???
				} else {
					return new RetrievePageResult(getService(action.getProjectId()).retrievePage(action.getPageId()));
				}
			} else if (action.getPagePath() != null) {
				if (action.isRenderContent()) {
					return new RetrievePageResult(getService(action.getProjectId()).retrieveRenderedPageByPath(
							action.getPagePath()));
				} else {
					return new RetrievePageResult(getService(action.getProjectId()).retrievePageByPath(
							action.getPagePath()));
				}
			}
		} catch (EntityNotFoundException e) {
			handle(e);
		}
		throw new IllegalStateException();
	}

}
