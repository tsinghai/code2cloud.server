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
package com.tasktop.c2c.server.common.service;

import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * Extend so that we can configure for polymorphic types. OM is not spring DI-able.
 */
public class JacksonJsonObjectMapper extends ObjectMapper {
	public JacksonJsonObjectMapper() {
		// FIXME this does not seem to work. Using annotations instead
		// enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, As.PROPERTY);
		getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		getJsonFactory().configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		getSerializationConfig().set(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

	}
}
