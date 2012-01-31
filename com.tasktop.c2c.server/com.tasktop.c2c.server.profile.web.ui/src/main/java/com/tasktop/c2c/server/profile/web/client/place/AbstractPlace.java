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
package com.tasktop.c2c.server.profile.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.Notifier;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 * @param <T>
 */
public abstract class AbstractPlace extends Place implements DefaultPlace {

	protected Message displayOnArrival;
	protected Notifier notifier = ProfileGinjector.get.instance().getNotifier();

	public AbstractPlace displayOnArrival(Message displayOnArrival) {
		this.displayOnArrival = displayOnArrival;
		return this;
	}

	@Override
	public String getToken() {
		return "";
	}

	public abstract String getPrefix();

	public String getHref() {
		return Window.Location.createUrlBuilder().setHash(getHistoryToken()).buildString();
	}

	public String getHistoryToken() {
		StringBuilder sb = new StringBuilder();
		sb.append(getPrefix());
		String token = getToken();
		if (StringUtils.hasText(token)) {
			sb.append(":").append(token);
		}
		return sb.toString();
	}

	protected Message message = null;

	/**
	 * 
	 */
	public AbstractPlace() {
		super();
	}

	public AbstractPlace setMessage(Message message) {
		this.message = message;
		return this;
	}

	protected Message getMessage() {
		if (message == null) {
			message = new Message(0, "Loading...", Message.MessageType.PROGRESS);
		}
		return message;
	}

	protected void reset() {

	}

	@Override
	public String toString() {
		return getHistoryToken();
	}

	protected void onNotAuthorised() {
		if (AuthenticationHelper.isAnonymous()) {
			// This place needs to be reset if it is going to be used again
			reset();
			SignInPlace.createPlace(this).go();
		} else {
			Message message = Message.createErrorMessage(getNotAuthorizedMessage());
			if (AuthenticationHelper.isAccountDisabled()) {
				ProfileGinjector.get.instance().getNotificationPanel().displayMessage(message);
			} else {
				AbstractPlace place = ProjectsDiscoverPlace.createPlace();
				place.displayOnArrival(message);
				place.go();
			}
		}
	}

	private String getNotAuthorizedMessage() {
		if (ProfileGinjector.get.instance().getAppState().getCredentials().getProfile().getAccountDisabled()) {
			return "Your account has been disabled";
		}
		return "You do not have permissions to visit the page";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getHref().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPlace other = (AbstractPlace) obj;
		return getHref().equals(other.getHref());
	}

}
