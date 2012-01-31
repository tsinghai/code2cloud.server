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
package com.tasktop.c2c.server.common.service.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Ignore;
import org.junit.Test;

import com.tasktop.c2c.server.common.service.JacksonJsonObjectMapper;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;


public class JacksonJsonObjectMapperTest {

	public static class Container {
		private Criteria criteria;

		public Criteria getCriteria() {
			return criteria;
		}

		public void setCriteria(Criteria criteria) {
			this.criteria = criteria;
		}
	}

	public static class Foo {
		private String bar;

		public void setBar(String bar) {
			this.bar = bar;
		}

		public String getBar() {
			return bar;
		}
	}

	// This demonstrates issue with interface typed field.
	@Test
	public void testCriteria() throws JsonGenerationException, JsonMappingException, IOException {
		JacksonJsonObjectMapper mapper = new JacksonJsonObjectMapper();
		Criteria crit = new ColumnCriteria("Test", "FOO");
		Container container = new Container();
		container.setCriteria(crit);
		String json = mapper.writeValueAsString(container);
		System.out.println(json);
		Container fromJson = mapper.readValue(json, Container.class);
		Assert.assertEquals(ColumnCriteria.class, fromJson.getCriteria().getClass());
	}

	@Test
	public void testDeserializeWithUnknownField() throws JsonParseException, JsonMappingException, IOException {
		JacksonJsonObjectMapper mapper = new JacksonJsonObjectMapper();
		String json = "{ \"bar\": \"barValue\", \"noSuchProperty\":\"val\" }";
		Foo foo = mapper.readValue(json, Foo.class);
		Assert.assertEquals("barValue", foo.getBar());
	}

	// This demonstrates issue with complex object in object'type field.
	@Ignore
	@Test
	public void testCriteriaWithComplexObject() throws JsonGenerationException, JsonMappingException, IOException {
		JacksonJsonObjectMapper mapper = new JacksonJsonObjectMapper();
		Criteria crit = new ColumnCriteria("Test", new Region(1, 10));

		Container container = new Container();
		container.setCriteria(crit);
		String json = mapper.writeValueAsString(container);
		System.out.println(json);
		Container fromJson = mapper.readValue(json, Container.class);
		Assert.assertEquals(ColumnCriteria.class, fromJson.getCriteria().getClass());
		Assert.assertEquals(Region.class, ((ColumnCriteria) fromJson.getCriteria()).getColumnValue().getClass());
	}

	// This is how the spring view serializes model result
	// FIXME task 336 can fix by setting serialization in the mapper, but that breaks elsewhere in spring.
	@Ignore
	@Test
	public void testSerializationWhenInMap() throws JsonGenerationException, JsonMappingException, IOException {
		JacksonJsonObjectMapper mapper = new JacksonJsonObjectMapper();

		ColumnCriteria crit = new ColumnCriteria("Test", "FOO");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("criteria", crit);
		String json = mapper.writeValueAsString(map);
		System.out.println(json);
		Container fromJson = mapper.readValue(json, Container.class);
		Assert.assertEquals(ColumnCriteria.class, fromJson.getCriteria().getClass());
	}

	// Task-873
	@Test
	public void testWithUnquotedControlChars() throws JsonParseException, JsonMappingException, IOException {
		JacksonJsonObjectMapper mapper = new JacksonJsonObjectMapper();
		String controlCharString = "TEST:\n\t\t\tTEST";
		String json = "{ \"bar\": \"" + controlCharString + "\", \"noSuchProperty\":\"val\" }";
		Foo foo = mapper.readValue(json, Foo.class);
		Assert.assertEquals(controlCharString, foo.getBar());
	}
}
