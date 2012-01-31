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
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.tasks.shared.action.DeleteProductAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteProductResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class DeleteProductActionHandler extends AbstractTaskActionHandler<DeleteProductAction, DeleteProductResult> {

	@Override
	public DeleteProductResult execute(DeleteProductAction action, ExecutionContext context) throws DispatchException {
		try {
			getService(action.getProjectId()).deleteProduct(action.getProductId());
			return new DeleteProductResult(action.getProductId());
		} catch (EntityNotFoundException e) {
			handle(e);
		} catch (ValidationException e) {
			handle(e);
		}
		throw new IllegalStateException();
	}
}
