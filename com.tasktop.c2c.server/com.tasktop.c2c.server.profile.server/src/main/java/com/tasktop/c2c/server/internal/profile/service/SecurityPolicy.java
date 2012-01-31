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
package com.tasktop.c2c.server.internal.profile.service;

import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;

public interface SecurityPolicy {

	/**
	 * create the object
	 */
	public void create(Object target) throws InsufficientPermissionsException;

	/**
	 * modify the object
	 */
	public void modify(Object target) throws InsufficientPermissionsException;

	/**
	 * delete an object
	 */
	public void delete(Object target) throws InsufficientPermissionsException;

	/**
	 * retrieve an object
	 */
	public void retrieve(Object target) throws InsufficientPermissionsException;

	/**
	 * add a related object to
	 */
	public void add(Object parent, Object child, String path) throws InsufficientPermissionsException;

	/**
	 * remove a related object from
	 */
	public void remove(Object parent, Object child, String path) throws InsufficientPermissionsException;
}
