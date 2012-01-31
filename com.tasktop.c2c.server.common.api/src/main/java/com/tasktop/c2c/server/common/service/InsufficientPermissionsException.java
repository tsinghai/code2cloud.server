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

public class InsufficientPermissionsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InsufficientPermissionsException() {
		super();
	}

	public InsufficientPermissionsException(String message, Throwable cause) {
		super(message, cause);
	}

	public InsufficientPermissionsException(String message) {
		super(message);
	}

	public InsufficientPermissionsException(Throwable cause) {
		super(cause);
	}

}
