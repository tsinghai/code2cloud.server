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
package com.tasktop.c2c.server.common.service;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.common.service.ValidationException;

public class AbstractJpaServiceBean {

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	protected Validator validator;

	@Autowired
	protected MessageSource messageSource;

	/**
	 * perform validation on the given object with the default validator
	 * 
	 * @param target
	 *            the object to be validated
	 */
	protected void validate(Object target) throws ValidationException {
		validate(target, validator);
	}

	/**
	 * perform validation on the given object with the specified validators. Note that the default validator is not used
	 * unless specified.
	 * 
	 * @param target
	 *            the object to be validated
	 * @param validators
	 *            the validators to use
	 * @throws ValidationException
	 */
	protected void validate(Object target, Validator... validators) throws ValidationException {
		Errors validationResult = createErrors(target);
		for (Validator validator : validators) {
			validator.validate(target, validationResult);
		}
		if (validationResult.hasErrors()) {
			throw new ValidationException(validationResult, messageSource);
		}
	}

	/**
	 * perform validation on the given object with the specified validators. Note that the default validator is not used
	 * unless specified.
	 * 
	 * @param target
	 *            the object to be validated
	 * @param validators
	 *            the validators to use
	 * @throws ValidationException
	 */
	protected void validate(Object target, Collection<Validator> validators) throws ValidationException {
		validate(target, validators.toArray(new Validator[validators.size()]));
	}

	/**
	 * Create a new errors for the given object
	 * 
	 * @param target
	 *            the object for which errors should be produced
	 */
	protected Errors createErrors(Object target) {
		String beanName = computeBeanName(target);
		return new BeanPropertyBindingResult(target, beanName);
	}

	private String computeBeanName(Object target) {
		Class<?> clazz = target.getClass();
		// in case of subclassing
		for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
			if (c.getAnnotation(Entity.class) != null) {
				clazz = c;
				break;
			}
		}
		return clazz.getSimpleName().toLowerCase();
	}

}
