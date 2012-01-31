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
package com.tasktop.c2c.server.tasks.client.widgets.admin.products;

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;

public class ProjectAdminTasksUtil {

	public static Product duplicateProduct(Product origProduct) {
		Product dupeProduct = new Product();
		dupeProduct.setName(origProduct.getName());
		dupeProduct.setDescription(origProduct.getDescription());
		dupeProduct.setDefaultMilestone(origProduct.getDefaultMilestone());
		dupeProduct.setId(origProduct.getId());
		dupeProduct.setIsActive(origProduct.getIsActive());

		dupeProduct.setComponents(duplicateComponents(origProduct.getComponents()));
		dupeProduct.setMilestones(duplicateMilestones(origProduct.getMilestones()));

		return dupeProduct;
	}

	public static List<Milestone> duplicateMilestones(List<Milestone> milestones) {
		List<Milestone> duplicates = new ArrayList<Milestone>();
		for (Milestone milestone : milestones) {
			duplicates.add(duplicateMilestone(milestone));
		}
		return duplicates;
	}

	public static Milestone duplicateMilestone(Milestone milestone) {
		Milestone duplicate = new Milestone();
		duplicate.setValue(milestone.getValue());
		duplicate.setProduct(milestone.getProduct());
		duplicate.setSortkey(milestone.getSortkey());
		duplicate.setId(milestone.getId());
		return duplicate;
	}

	public static List<Component> duplicateComponents(List<Component> components) {
		List<Component> duplicates = new ArrayList<Component>();
		for (Component component : components) {
			duplicates.add(duplicateComponent(component));
		}
		return duplicates;
	}

	public static Component duplicateComponent(Component component) {
		Component duplicate = new Component();
		duplicate.setDescription(component.getDescription());
		duplicate.setInitialOwner(component.getInitialOwner());
		duplicate.setName(component.getName());
		duplicate.setId(component.getId());
		duplicate.setProduct(component.getProduct());
		return duplicate;
	}

	public static void neutralizeTemporaryIds(Product product) {
		if (product.getId() != null && product.getId() < 0) {
			product.setId(null);
		}

		for (Milestone milestone : product.getMilestones()) {
			if (milestone.getId() != null && milestone.getId() < 0) {
				milestone.setId(null);
			}
		}

		for (Component component : product.getComponents()) {
			if (component.getId() != null && component.getId() < 0) {
				component.setId(null);
			}
		}
	}

}
