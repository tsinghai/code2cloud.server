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
package com.tasktop.c2c.server.common.service.tests.domain.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;

public class CriteriaParserTest {

	@Test
	public void testParse_ColumnCriteria_IntegerValue() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, 1);
		assertCriteriaParses(columnCriteria);
	}

	@Test
	public void testParse_ColumnCriteria_DateValue() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, new Date());
		assertCriteriaParses(columnCriteria);
	}

	@Test
	public void testParse_ColumnCriteria_StringValue() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, "Bar");
		assertCriteriaParses(columnCriteria);
	}

	@Test
	public void testParse_NaryCriteria_2() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, 1);
		ColumnCriteria columnCriteria2 = new ColumnCriteria("Foo", Operator.EQUALS, "Bar");
		NaryCriteria naryCriteria = new NaryCriteria(Operator.AND, columnCriteria, columnCriteria2);
		assertCriteriaParses(naryCriteria);
	}

	@Test
	public void testParse_NaryCriteria_3() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, 1);
		ColumnCriteria columnCriteria2 = new ColumnCriteria("Foo", Operator.EQUALS, "Bar");
		ColumnCriteria columnCriteria3 = new ColumnCriteria("Foo", Operator.EQUALS, "Baz");
		NaryCriteria naryCriteria = new NaryCriteria(Operator.OR, columnCriteria2, columnCriteria3);
		NaryCriteria naryCriteria2 = new NaryCriteria(Operator.AND, columnCriteria, naryCriteria);
		assertCriteriaParses(naryCriteria2);
	}

	@Test
	public void testParse_NaryCriteria_4() {
		ColumnCriteria columnCriteria = new ColumnCriteria("Foo", Operator.EQUALS, 1);
		ColumnCriteria columnCriteria2 = new ColumnCriteria("Foo", Operator.NOT_EQUALS, "Bar");
		ColumnCriteria columnCriteria3 = new ColumnCriteria("Foo", Operator.STRING_CONTAINS, "Baz");
		NaryCriteria naryCriteria = new NaryCriteria(Operator.OR, columnCriteria2, columnCriteria3);
		NaryCriteria naryCriteria2 = new NaryCriteria(Operator.AND, columnCriteria, naryCriteria);
		assertCriteriaParses(naryCriteria2);
	}

	@Test
	public void testParse_NaryCriteria_5_task2966() {
		ColumnCriteria columnCriteria = new ColumnCriteria("A", Operator.EQUALS, 1);
		ColumnCriteria columnCriteria2 = new ColumnCriteria("B", Operator.NOT_EQUALS, 2);
		ColumnCriteria columnCriteria3 = new ColumnCriteria("C", Operator.EQUALS, 3);
		ColumnCriteria columnCriteria4 = new ColumnCriteria("D", Operator.NOT_EQUALS, 4);
		NaryCriteria naryCriteria = new NaryCriteria(Operator.AND, columnCriteria, columnCriteria2);
		NaryCriteria naryCriteria2 = new NaryCriteria(Operator.AND, columnCriteria3, columnCriteria4);
		NaryCriteria naryCriteria3 = new NaryCriteria(Operator.OR, naryCriteria, naryCriteria2);
		assertCriteriaParses(naryCriteria3);
	}

	private void assertCriteriaParses(Criteria criteria) {
		String queryString = criteria.toQueryString();
		System.out.println("Parsing " + queryString);
		Criteria parsed = CriteriaParser.parse(queryString);
		assertNotNull(parsed);
		assertEquals(criteria, parsed);
	}

}
