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
package com.tasktop.c2c.server.tasks.client.presenters;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;


public class TaskCriteriaQuery {

	public Criteria computeCriteria(Map<String, List<String>> parameters) {
		NaryCriteria base = new NaryCriteria();
		base.setOperator(Operator.AND);

		for (Entry<String, List<String>> entry : parameters.entrySet()) {
			NaryCriteria criteria = new NaryCriteria();
			criteria.setOperator(Operator.OR);
			for (String value : entry.getValue()) {
				value = value.trim();
				if (value.length() == 0) {
					continue;
				}
				// FIXME: operator CONTAINS/EQUALS
				ColumnCriteria column = new ColumnCriteria(entry.getKey(), Operator.EQUALS, value);
				criteria.addSubCriteria(column);
			}
			if (!criteria.getSubCriteria().isEmpty()) {
				if (criteria.getSubCriteria().size() == 1) {
					base.addSubCriteria(criteria.getSubCriteria().get(0));
				} else {
					base.addSubCriteria(criteria);
				}
			}
		}
		return base;
	}

}
