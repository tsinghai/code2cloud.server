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
package com.tasktop.c2c.server.web.proxy;

public class HeaderFilter {

	private HeaderFilter next;

	public HeaderFilter() {
	}

	public String processRequestHeader(String headerName, String headerValue) {
		if (next != null) {
			return next.processRequestHeader(headerName, headerValue);
		}
		return headerValue;
	}

	public String processResponseHeader(String headerName, String headerValue) {
		if (next != null) {
			return next.processResponseHeader(headerName, headerValue);
		}
		return headerValue;
	}

	public HeaderFilter getNext() {
		return next;
	}

	public void setNext(HeaderFilter next) {
		this.next = next;
	}

}
