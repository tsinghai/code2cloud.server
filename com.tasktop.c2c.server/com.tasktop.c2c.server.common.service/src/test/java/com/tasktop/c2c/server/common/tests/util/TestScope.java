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
package com.tasktop.c2c.server.common.tests.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class TestScope implements Scope {

	private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<String, Runnable>();

	private Map<String, Object> objects = new HashMap<String, Object>();

	public Object get(String name, ObjectFactory<?> objectFactory) {
		Object scopedObject = objects.get(name);
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			objects.put(name, scopedObject);
		}
		return scopedObject;
	}

	public Object remove(String name) {
		Object scopedObject = objects.get(name);
		if (scopedObject != null) {
			objects.remove(name);
			this.destructionCallbacks.remove(name);
			return scopedObject;
		} else {
			return null;
		}
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		this.destructionCallbacks.put(name, callback);
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	public String getConversationId() {
		return null;
	}

	/**
	 * Invoke all registered destruction callbacks. To be called on
	 * ServletContext shutdown.
	 * 
	 * @see org.springframework.web.context.ContextCleanupListener
	 */
	public void destroy() {
		for (Runnable runnable : this.destructionCallbacks.values()) {
			runnable.run();
		}
		this.destructionCallbacks.clear();
	}

}
