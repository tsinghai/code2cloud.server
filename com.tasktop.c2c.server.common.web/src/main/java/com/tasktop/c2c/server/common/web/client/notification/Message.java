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

import java.util.Arrays;
import java.util.List;

public class Message {

	public static enum MessageType {
		PROGRESS, SUCCESS, ERROR
	}

	public static class Action {
		private final String message;
		private final Runnable handler;

		public Action(String message, Runnable handler) {
			this.message = message;
			this.handler = handler;
		}

		public String getMessage() {
			return message;
		}

		public Runnable getHandler() {
			return handler;
		}
	}

	private Integer displayFor = 0;
	private String mainMessage;
	private List<Action> actions;

	private MessageType messageType;
	private boolean scheduledForRemoval = false;
	private boolean scheduledForDisplay = false;

	public Message(Integer displayFor, String message, MessageType messageType, Action... actions) {
		this.displayFor = displayFor;
		this.mainMessage = message;
		this.messageType = messageType;
		this.actions = Arrays.asList(actions);
	}

	public static Message createErrorMessage(String message) {
		return new Message(0, message, MessageType.ERROR);
	}

	public static Message createProgressMessage(String message) {
		return new Message(0, message, MessageType.PROGRESS);
	}

	public static Message createSuccessMessage(String message) {
		return new Message(10, message, MessageType.SUCCESS);
	}

	public static Message createSuccessWithActionMessage(String message, Action... actions) {
		return new Message(30, message, MessageType.SUCCESS, actions);
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public Integer getDisplayFor() {
		return displayFor;
	}

	public String getMainMessage() {
		return mainMessage;
	}

	public boolean isScheduledForRemoval() {
		return scheduledForRemoval;
	}

	public void setScheduledForRemoval(boolean scheduledForRemoval) {
		this.scheduledForRemoval = scheduledForRemoval;
	}

	public boolean isScheduledForDisplay() {
		return scheduledForDisplay;
	}

	public void setScheduledForDisplay(boolean scheduledForDisplay) {
		this.scheduledForDisplay = scheduledForDisplay;
	}

	/**
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}

}
