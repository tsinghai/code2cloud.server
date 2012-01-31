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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;

@Component
public class MilestoneConverter implements ObjectConverter<Milestone> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Milestone.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(Milestone target, Object internalObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Milestone source = (com.tasktop.c2c.server.internal.tasks.domain.Milestone) internalObject;

		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setValue(source.getValue());
		target.setSortkey(source.getSortkey());

		if (!context.isThin()) {
			// SHALLOW copy.
			Product targetProduct = new Product();
			if (source.getProduct().getId() == null) {
				targetProduct.setId(null);
			} else {
				targetProduct.setId(Integer.valueOf(source.getProduct().getId()));
			}
			target.setProduct(targetProduct);
		}
	}

	@Override
	public Class<Milestone> getTargetClass() {
		return Milestone.class;
	}

	public static Milestone copy(com.tasktop.c2c.server.internal.tasks.domain.Milestone source) {
		Milestone target = new Milestone();
		new MilestoneConverter().copy(target, source, null, null);
		return target;
	}

}
