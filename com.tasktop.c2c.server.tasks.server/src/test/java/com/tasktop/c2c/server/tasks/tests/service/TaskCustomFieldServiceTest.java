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
package com.tasktop.c2c.server.tasks.tests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.internal.tasks.service.TaskCustomFieldService;
import com.tasktop.c2c.server.internal.tasks.service.sql.SqlDialect;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.tests.service.sql.AbstractSqlTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class TaskCustomFieldServiceTest extends AbstractSqlTest {

	@Autowired
	private TaskCustomFieldService service;

	@Autowired
	private SqlDialect sqlDialect;

	private Set<FieldDescriptor> createdFields = new HashSet<FieldDescriptor>();

	@After
	public void after() {
		for (FieldDescriptor descriptor : createdFields) {
			try {
				service.removeCustomField(descriptor);
			} catch (Throwable t) {
				// ignore
				t.printStackTrace();
			}
		}
	}

	@Test
	public void testGetFieldValues_SingleSelect() throws ValidationException, EntityNotFoundException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("Test");
		descriptor.setFieldType(FieldType.SINGLE_SELECT);
		descriptor.setName("test");
		List<String> originalValues = Arrays.asList("one", "two", "three");
		descriptor.setValueStrings(originalValues);

		createdFields.add(descriptor);

		service.createCustomField(descriptor);

		List<String> fieldValues = fromCustomFieldValues(service.getFieldValues(descriptor));
		assertTrue("Field values expected to contain " + originalValues, fieldValues.containsAll(originalValues));
		assertTrue("Field values expected to contain ---", fieldValues.contains("---"));
		assertEquals(originalValues.size() + 1, fieldValues.size());
	}

	private List<String> fromCustomFieldValues(List<CustomFieldValue> values) {
		List<String> result = new ArrayList<String>(values.size());
		for (CustomFieldValue value : values) {
			result.add(value.getValue());
		}
		return result;
	}

	@Test
	public void testGetFieldValues_MultiSelect() throws ValidationException, EntityNotFoundException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("Test");
		descriptor.setFieldType(FieldType.MULTI_SELECT);
		descriptor.setName("test");
		List<String> originalValues = Arrays.asList("one", "two", "three");
		descriptor.setValueStrings(originalValues);

		createdFields.add(descriptor);

		service.createCustomField(descriptor);

		List<String> fieldValues = fromCustomFieldValues(service.getFieldValues(descriptor));
		assertTrue("Field values expected to contain " + originalValues, fieldValues.containsAll(originalValues));
		assertTrue("Field values expected to contain ---", fieldValues.contains("---"));
		assertEquals(originalValues.size() + 1, fieldValues.size());
	}

	@Test
	public void testGetFieldValuesNoField() {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setName("test2");
		try {
			service.getFieldValues(descriptor);
			fail("Expected that field would not exist " + descriptor.getName());
		} catch (EntityNotFoundException e) {
			// expected
		}
	}

}
