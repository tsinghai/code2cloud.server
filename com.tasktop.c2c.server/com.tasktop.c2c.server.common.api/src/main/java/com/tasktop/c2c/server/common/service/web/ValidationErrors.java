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

public class ValidationErrors {
	public static class ValidationError {
		private String objectName;
		private String fieldName;
		private String errorCode;
		private String[] args;
		private String defaultMessage;

		public ValidationError() {
		}

		public ValidationError(String objectName, String fieldName, String errorCode, String[] args,
				String defaultMessage) {
			this.objectName = objectName;
			this.fieldName = fieldName;
			this.errorCode = errorCode;
			this.args = args;
			this.defaultMessage = defaultMessage;
		}

		public String getObjectName() {
			return objectName;
		}

		public void setObjectName(String objectName) {
			this.objectName = objectName;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		public String[] getArgs() {
			return args;
		}

		public void setArgs(String[] args) {
			this.args = args;
		}

		public String getDefaultMessage() {
			return defaultMessage;
		}

		public void setDefaultMessage(String defaultMessage) {
			this.defaultMessage = defaultMessage;
		}

	}

	private String objectName;
	private List<ValidationError> errors = new ArrayList<ValidationError>();

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void addError(String objectName, String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		String[] args = new String[errorArgs == null ? 0 : errorArgs.length];
		if (errorArgs != null) {
			int index = -1;
			for (Object arg : errorArgs) {
				++index;
				args[index] = errorArgs[index] == null ? null : errorArgs[index].toString();
			}
		}
		errors.add(new ValidationError(objectName, field, errorCode, args, defaultMessage));
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	public void setErrors(List<ValidationError> errors) {
		this.errors = errors;
	}

}
