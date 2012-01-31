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
package com.tasktop.c2c.server.common.web.shared;

import java.util.List;

public class ValidationFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	private List<String> messages;

	public ValidationFailedException(List<String> messages) {
		this.messages = messages;
	}

	public ValidationFailedException() {
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	@Override
	public String getMessage() {
		String messageString = messages.toString().substring(1);
		return messageString.substring(0, messageString.length() - 1);
	}

}
