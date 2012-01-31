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
package com.tasktop.c2c.server.cloud.service;

import java.util.Date;

/**
 * The result of requesting a build slave. We can either get a slave ip that we may start using, or a promise token we
 * can use to ask for a new slave later.
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class RequestBuildSlaveResult {

	public static RequestBuildSlaveResult forSlave(String ip, Date dueDate) {
		RequestBuildSlaveResult result = new RequestBuildSlaveResult();
		result.setType(Type.SLAVE);
		result.setSlaveIp(ip);
		result.setSlaveDueDate(dueDate);
		return result;
	}

	public static RequestBuildSlaveResult forPromise(String promiseToken) {
		RequestBuildSlaveResult result = new RequestBuildSlaveResult();
		result.setType(Type.PROMISE);
		result.setPromiseToken(promiseToken);
		return result;
	}

	public static RequestBuildSlaveResult forReject() {
		RequestBuildSlaveResult result = new RequestBuildSlaveResult();
		result.setType(Type.REJECTED);
		return result;
	}

	public enum Type {
		/** A new slave is returned. */
		SLAVE,
		/** A promise is returned that may be used to ask for a slave in the near future. */
		PROMISE,
		/** The request was rejected. This happens when asking to renew a slave. */
		REJECTED;
	};

	private Type type;
	// if type is slave
	private String slaveIp;
	private Date slaveDueDate;
	// if type is promise
	private String promiseToken;

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the slaveIp
	 */
	public String getSlaveIp() {
		return slaveIp;
	}

	/**
	 * @param slaveIp
	 *            the slaveIp to set
	 */
	public void setSlaveIp(String slaveIp) {
		this.slaveIp = slaveIp;
	}

	/**
	 * @return the slaveDueDate
	 */
	public Date getSlaveDueDate() {
		return slaveDueDate;
	}

	/**
	 * @param slaveDueDate
	 *            the slaveDueDate to set
	 */
	public void setSlaveDueDate(Date slaveDueDate) {
		this.slaveDueDate = slaveDueDate;
	}

	/**
	 * @return the promiseToken
	 */
	public String getPromiseToken() {
		return promiseToken;
	}

	/**
	 * @param promiseToken
	 *            the promiseToken to set
	 */
	public void setPromiseToken(String promiseToken) {
		this.promiseToken = promiseToken;
	}

	@Override
	public String toString() {
		if (type == null) {
			return super.toString();
		}
		switch (type) {
		case PROMISE:
			return "{PROMISE: " + promiseToken + "}";
		case REJECTED:
			return "REJECTED";
		case SLAVE:
			return "{Slave: "
					+ slaveIp
					+ ", expiry: "
					+ (slaveDueDate == null ? "NONE"
							: ((slaveDueDate.getTime() - System.currentTimeMillis()) / (1000 * 60)) + "min") + "}";
		default:
			return super.toString();
		}
	}
}
