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
package com.tasktop.c2c.server.common.web.shared;

/**
 * Marker interface representing a read action that is cache-able. It will be invalidated by any WriteAction.
 * Sub-classes should implement equals and hashcode so that results can be tested for equality.
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public interface CachableReadAction {

}
