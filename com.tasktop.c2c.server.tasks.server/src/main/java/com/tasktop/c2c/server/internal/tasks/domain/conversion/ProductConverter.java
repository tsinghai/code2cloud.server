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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tasktop.c2c.server.internal.tasks.domain.Version;
import com.tasktop.c2c.server.tasks.domain.Product;

@org.springframework.stereotype.Component
public class ProductConverter implements ObjectConverter<Product> {

	@Autowired
	private DomainConverter domainConverter;

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Product.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(Product target, Object internalObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Product source = (com.tasktop.c2c.server.internal.tasks.domain.Product) internalObject;

		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setDescription(source.getDescription());
		target.setIsActive(source.getIsactive());
		target.setName(source.getName());

		if (!context.isThin()) {
			// set the default milestone if we have one.
			if (source.getDefaultmilestone() != null) {
				for (com.tasktop.c2c.server.internal.tasks.domain.Milestone milestone : source.getMilestones()) {
					if (milestone.getValue().equals(source.getDefaultmilestone())) {
						target.setDefaultMilestone((com.tasktop.c2c.server.tasks.domain.Milestone) converter
								.convert(milestone, context));
						break;
					}
				}
			}

			target.setComponents((List<com.tasktop.c2c.server.tasks.domain.Component>) converter.convert(
					source.getComponents(), context));

			// TODO make a better choice for default component.
			if (!target.getComponents().isEmpty()) {
				target.setDefaultComponent(target.getComponents().iterator().next());
			}

			target.setMilestones((List<com.tasktop.c2c.server.tasks.domain.Milestone>) converter.convert(
					source.getMilestones(), context));

			target.setReleaseTags(new ArrayList<String>(source.getVersionses().size()));
			for (Version v : source.getVersionses()) {
				target.getReleaseTags().add(v.getValue());
			}
		}
	}

	@Override
	public Class<Product> getTargetClass() {
		return Product.class;
	}

}
