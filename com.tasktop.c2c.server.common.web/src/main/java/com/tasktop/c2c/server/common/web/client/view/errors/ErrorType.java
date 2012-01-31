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
package com.tasktop.c2c.server.common.web.client.view.errors;

public enum ErrorType {

	ERROR404("four-oh-four", "File Not Found"),
	ERROR500("five-hundred", "An application error occurred"),
	ERROR503("five-oh-three", "We're currently performing maintenance");

	private String errorImageClass;
	private String errorDescription;

	ErrorType(String errorImageClass, String errorDescription) {
		this.errorImageClass = errorImageClass;
		this.errorDescription = errorDescription;
	}

	public String getErrorImageClass() {
		return errorImageClass;
	}

	public String getErrorDescription() {
		return errorDescription;
	}
}
