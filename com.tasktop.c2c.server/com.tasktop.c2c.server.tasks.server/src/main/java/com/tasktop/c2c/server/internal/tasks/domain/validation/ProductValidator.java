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
package com.tasktop.c2c.server.internal.tasks.domain.validation;

import java.util.List;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Product;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class ProductValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Product.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		// Ensure that the default milestone is in the set of associated milestones on the internal DO.
		Product p = ((Product) target);
		List<Milestone> milestones = p.getMilestones();
		Boolean inMilestones = false;
		for (Milestone milestone : milestones) {
			if (p.getDefaultmilestone().equals(milestone.getValue())) {
				inMilestones = true;
				break;
			}
		}
		if (!inMilestones) {
			errors.reject("defaultMilestone.notInAssociatedMilestones");
		}
	}
}
