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
package com.tasktop.c2c.server.common.service.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.common.service.web.ValidationErrors.ValidationError;


public class ValidationErrorsBridge extends AbstractErrors {

	private ValidationErrors delegate;

	public ValidationErrorsBridge(ValidationErrors validationErrors) {
		delegate = validationErrors;
	}

	public String getObjectName() {
		return delegate.getObjectName();
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		delegate.addError(delegate.getObjectName(), null, errorCode, errorArgs, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		delegate.addError(delegate.getObjectName(), field, errorCode, errorArgs, defaultMessage);
	}

	public void addAllErrors(Errors errors) {
		for (ObjectError error : errors.getAllErrors()) {
			String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : null;
			delegate.addError(error.getObjectName(), fieldName, error.getCode(), error.getArguments(),
					error.getDefaultMessage());
		}
	}

	public List<ObjectError> getGlobalErrors() {
		List<ObjectError> errors = new ArrayList<ObjectError>();
		for (ValidationError error : delegate.getErrors()) {
			if (error.getFieldName() == null) {
				errors.add(new ObjectError(error.getObjectName(), new String[] { error.getErrorCode() }, error
						.getArgs(), error.getDefaultMessage()));
			}
		}
		return errors;
	}

	public List<FieldError> getFieldErrors() {
		List<FieldError> errors = new ArrayList<FieldError>();
		for (ValidationError error : delegate.getErrors()) {
			if (error.getFieldName() != null) {
				String[] errorCodes = computeErrorCodes(error);
				errors.add(new FieldError(error.getObjectName(), error.getFieldName(), null, false, errorCodes, error
						.getArgs(), error.getDefaultMessage()));
			}
		}
		return errors;
	}

	private String[] computeErrorCodes(ValidationError error) {
		// [nonUnique.page.path, nonUnique.path, nonUnique.java.lang.String, nonUnique]
		List<String> codes = new ArrayList<String>(5);

		if (error.getFieldName() != null) {
			if (error.getObjectName() != null) {
				codes.add(error.getErrorCode() + '.' + error.getObjectName() + '.' + error.getFieldName());
			}
			codes.add(error.getErrorCode() + '.' + error.getFieldName());
		}
		codes.add(error.getErrorCode());

		return codes.toArray(new String[codes.size()]);
	}

	public Object getFieldValue(String field) {
		return null;
	}

}
