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
package com.tasktop.c2c.server.profile.domain.project;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class NotificationSettings extends AbstractEntity {
	private Boolean emailTaskActivity;
	private Boolean emailNewsAndEvents;
	private Boolean emailServiceAndMaintenance;

	/**
	 * @return the emailTaskActivity
	 */
	public Boolean getEmailTaskActivity() {
		return emailTaskActivity;
	}

	/**
	 * @param emailTaskActivity
	 *            the emailTaskActivity to set
	 */
	public void setEmailTaskActivity(Boolean emailTaskActivity) {
		this.emailTaskActivity = emailTaskActivity;
	}

	/**
	 * @return the emailNewsAndEvents
	 */
	public Boolean getEmailNewsAndEvents() {
		return emailNewsAndEvents;
	}

	/**
	 * @param emailNewsAndEvents
	 *            the emailNewsAndEvents to set
	 */
	public void setEmailNewsAndEvents(Boolean emailNewsAndEvents) {
		this.emailNewsAndEvents = emailNewsAndEvents;
	}

	/**
	 * @return the emailServiceAndMaintenance
	 */
	public Boolean getEmailServiceAndMaintenance() {
		return emailServiceAndMaintenance;
	}

	/**
	 * @param emailServiceAndMaintenance
	 *            the emailServiceAndMaintenance to set
	 */
	public void setEmailServiceAndMaintenance(Boolean emailServiceAndMaintenance) {
		this.emailServiceAndMaintenance = emailServiceAndMaintenance;
	}
}
