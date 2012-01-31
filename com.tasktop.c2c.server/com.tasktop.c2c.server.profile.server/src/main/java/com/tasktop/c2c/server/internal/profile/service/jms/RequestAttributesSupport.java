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
package com.tasktop.c2c.server.internal.profile.service.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.context.request.AbstractRequestAttributes;

public class RequestAttributesSupport extends AbstractRequestAttributes {

	private Map<Integer, Map<String, Object>> attributesByScope = new HashMap<Integer, Map<String, Object>>();
	{
		attributesByScope.put(SCOPE_GLOBAL_SESSION, new HashMap<String, Object>());
		attributesByScope.put(SCOPE_SESSION, new HashMap<String, Object>());
		attributesByScope.put(SCOPE_REQUEST, new HashMap<String, Object>());
	}

	private String sessionId;

	@Override
	public Object getAttribute(String name, int scope) {
		return getScopedAttributes(scope).get(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		getScopedAttributes(scope).put(name, value);
	}

	@Override
	public void removeAttribute(String name, int scope) {
		getScopedAttributes(scope).remove(name);
		removeRequestDestructionCallback(name);
	}

	@Override
	public String[] getAttributeNames(int scope) {
		Map<String, Object> scopedAttributes = getScopedAttributes(scope);
		return scopedAttributes.keySet().toArray(new String[scopedAttributes.size()]);
	}

	private Map<String, Object> getScopedAttributes(int scope) {
		return attributesByScope.get(scope);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback, int scope) {
		registerRequestDestructionCallback(name, callback);
	}

	@Override
	public Object resolveReference(String key) {
		return null;
	}

	@Override
	public synchronized String getSessionId() {
		if (sessionId == null) {
			sessionId = UUID.randomUUID().toString();
		}
		return sessionId;
	}

	@Override
	public Object getSessionMutex() {
		return this;
	}

	@Override
	protected void updateAccessedSessionAttributes() {
		// nothing to do
	}

}
