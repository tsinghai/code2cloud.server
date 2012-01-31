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
package com.tasktop.c2c.server.tasks.tests.domain.mock;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.tasks.domain.Classification;
import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;


public class MockProductFactory {

	private static int created = 0;

	public static Product create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Product> create(EntityManager entityManager, int numProducts) {
		return create(entityManager, numProducts, 10);
	}

	public static List<Product> create(EntityManager entityManager, int numProducts, int numChildComponentsEach) {

		List<Product> mocks = new ArrayList<Product>(numProducts);
		for (int x = 0; x < numProducts; ++x) {
			Product mock = populate(new Product());
			if (entityManager != null) {
				entityManager.persist(mock);

				Profile defaultAssignee = MockProfileFactory.create(entityManager);
				List<Component> components = MockComponentFactory.createWithProduct(entityManager,
						numChildComponentsEach, mock);
				for (Component component : components) {
					component.setInitialOwner(defaultAssignee);
					defaultAssignee.getInitialOwnerComponents().add(component);
					component.setProduct(mock);
					mock.getComponents().add(component);
					entityManager.persist(component);
				}
				Milestone defaultMilestone = new Milestone();
				defaultMilestone.setProduct(mock);
				mock.getMilestones().add(defaultMilestone);
				defaultMilestone.setValue("---");
				defaultMilestone.setSortkey((short) 0);
				entityManager.persist(defaultMilestone);

				mock.setDefaultmilestone(defaultMilestone.getValue());
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Product populate(Product product) {
		int index = ++created;

		product.setName("Product" + index);
		product.setDescription("A product description " + index);
		product.setDefaultmilestone("---");
		product.setClassifications(createClassification(index));
		product.getClassifications().getProductses().add(product);

		return product;
	}

	private static Classification createClassification(Integer index) {
		Classification classifications = new Classification();
		classifications.setName("None" + index);
		return classifications;
	}

}
