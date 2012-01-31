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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

/**
 * Converts to a single type of object.
 * 
 * @param <T>
 *            the target object type
 */
public interface ObjectConverter<T> {

	boolean supportsSource(Class<?> sourceClass);

	public Class<T> getTargetClass();

	void copy(T targetObject, Object sourceObject, DomainConverter converter, DomainConversionContext context);
}
