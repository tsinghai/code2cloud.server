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

import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

@org.springframework.stereotype.Component
public class ComponentConverter implements ObjectConverter<Component> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Component.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(Component target, Object internalObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Component source = (com.tasktop.c2c.server.internal.tasks.domain.Component) internalObject;

		target.setId(source.getId() == null ? null : Integer.valueOf(source.getId()));
		target.setDescription(source.getDescription());
		target.setName(source.getName());

		target.setInitialOwner((TaskUserProfile) converter.convert(source.getInitialOwner(), context));

		if (!context.isThin()) {
			// SHALLOW copy.
			if (source.getProduct() != null) {
				Product targetProduct = new Product();
				if (source.getProduct().getId() == null) {
					targetProduct.setId(null);
				} else {
					targetProduct.setId(Integer.valueOf(source.getProduct().getId()));
				}
				target.setProduct(targetProduct);
			}
		}
	}

	@Override
	public Class<Component> getTargetClass() {
		return Component.class;
	}

	public static Component copy(com.tasktop.c2c.server.internal.tasks.domain.Component source) {
		Component target = new Component();
		new ComponentConverter().copy(target, source, null, null);
		return target;
	}
}
