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
package com.tasktop.c2c.server.common.service.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AbstractDomainValidator implements Validator {

	private Map<Class<?>, Validator> validatorByType = new HashMap<Class<?>, Validator>();

	public AbstractDomainValidator() {
	}

	public boolean supports(Class<?> clazz) {
		return computeValidator(clazz) != null;
	}

	public Map<Class<?>, Validator> getValidatorByType() {
		return Collections.unmodifiableMap(validatorByType);
	}

	protected void registerValidator(Class<?> clazz, Validator validator) {
		if (!validator.supports(clazz)) {
			throw new IllegalStateException();
		}
		validatorByType.put(clazz, validator);
	}

	private Validator computeValidator(Class<?> clazz) {
		Validator validator = validatorByType.get(clazz);
		if (validator == null) {
			for (Entry<Class<?>, Validator> candidate : validatorByType.entrySet()) {
				if (candidate.getKey().isAssignableFrom(clazz)) {
					validator = candidate.getValue();
					break;
				}
			}
		}
		return validator;
	}

	public void validate(Object target, Errors errors) {
		Validator validator = computeValidator(target.getClass());
		if (validator == null) {
			throw new IllegalArgumentException();
		}
		validator.validate(target, errors);
	}

}
