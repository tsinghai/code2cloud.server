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
package com.tasktop.c2c.server.common.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Throwing this event causes the client-side dispatch cache to be cleared.
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class ClearCacheEvent extends GwtEvent<ClearCacheEventHandler> {
	public static Type<ClearCacheEventHandler> TYPE = new Type<ClearCacheEventHandler>();

	public ClearCacheEvent() {
	}

	@Override
	protected void dispatch(ClearCacheEventHandler handler) {
		handler.doClearCache();
	}

	@Override
	public Type<ClearCacheEventHandler> getAssociatedType() {
		return TYPE;
	}

}
