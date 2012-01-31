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
package com.tasktop.c2c.server.internal.wiki.server.domain.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DomainConverter {

	@Autowired
	private List<ObjectConverter<?>> converters;

	@SuppressWarnings("unchecked")
	public Object convert(Object sourceObject, DomainConversionContext context) {
		if (sourceObject == null) {
			return null;
		}

		Object targetObject = context.getDomainObject(sourceObject);
		if (targetObject != null) {
			return targetObject;
		}

		if (sourceObject instanceof Collection<?>) {
			return convertCollection((Collection<?>) sourceObject, context);
		}

		@SuppressWarnings("rawtypes")
		ObjectConverter converter = getConverterFor(sourceObject.getClass());
		targetObject = getEmptyTargetObject(converter.getTargetClass());

		context.map(sourceObject, targetObject); // Put early/empty in case of cycles in sub-graph.

		converter.copy(targetObject, sourceObject, this, context);
		// context.add(domainObject);
		return targetObject;
	}

	// For now assume everything maps to list.
	private Object convertCollection(Collection<?> sourceObjects, DomainConversionContext context) {
		List<Object> target = new ArrayList<Object>(sourceObjects.size());
		for (Object sourceObject : sourceObjects) {
			target.add(convert(sourceObject, context));
		}
		return target;
	}

	private Object getEmptyTargetObject(Class<?> targetClass) {
		try {
			return targetClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private ObjectConverter<?> getConverterFor(Class<?> sourceClass) {
		for (ObjectConverter<?> converter : converters) {
			if (converter.supportsSource(sourceClass)) {
				return converter;
			}
		}
		throw new IllegalArgumentException("No converter registered for type: " + sourceClass.getName());
	}

	public void setConverters(List<ObjectConverter<?>> converters) {
		this.converters = converters;
	}
}
