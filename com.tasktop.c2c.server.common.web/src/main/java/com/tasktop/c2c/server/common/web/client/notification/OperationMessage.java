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
package com.tasktop.c2c.server.common.web.client.notification;

public class OperationMessage {

	private String inProgressText;
	private String errorText;
	private String successText;

	public OperationMessage(String inProgressText) {
		this.inProgressText = inProgressText;
	}

	public static OperationMessage create(String inProgressText) {
		return new OperationMessage(inProgressText);
	}

	public OperationMessage setInProgressText(String inProgressText) {
		this.inProgressText = inProgressText;
		return this;
	}

	public OperationMessage setErrorText(String errorText) {
		this.errorText = errorText;
		return this;
	}

	public OperationMessage setSuccessText(String successText) {
		this.successText = successText;
		return this;
	}

	public Message getErrorMessage() {
		if (errorText == null) {
			return null;
		}
		return new Message(0, errorText, Message.MessageType.ERROR);
	}

	public Message getSuccessMessage() {
		if (successText == null) {
			return null;
		}
		return new Message(10, successText, Message.MessageType.SUCCESS);
	}

	public Message getProgressMessage() {
		return new Message(0, inProgressText, Message.MessageType.PROGRESS);
	}
}
