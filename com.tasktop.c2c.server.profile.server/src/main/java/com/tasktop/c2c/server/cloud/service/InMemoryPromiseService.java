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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.ValidationException;

/**
 * TEMP In-Memory impl
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Component
public class InMemoryPromiseService implements PromiseService {

	class Promise {
		private String token;
		private Date expiry;

		public Promise(String token, Date expiry) {
			this.token = token;
			this.expiry = expiry;
		}

		public Promise() {

		}

		/**
		 * @return the token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * @param token
		 *            the token to set
		 */
		public void setToken(String token) {
			this.token = token;
		}

		/**
		 * @return the expiry
		 */
		public Date getExpiry() {
			return expiry;
		}

		/**
		 * @param expiry
		 *            the expiry to set
		 */
		public void setExpiry(Date expiry) {
			this.expiry = expiry;
		}
	}

	private Queue<Promise> promiseTokens = new LinkedList<Promise>();

	private void cullExpiredPromisis() {
		Date now = new Date();
		Iterator<Promise> it = promiseTokens.iterator();
		while (it.hasNext()) {
			Promise p = it.next();
			if (p.expiry.before(now)) {
				it.remove();
			}
		}

	}

	@Override
	public synchronized boolean hasOutstandingPromises() {
		return getNumberOfOutstandingPromises() != 0;
	}

	@Override
	public synchronized String getNewPromiseToken(long durationInMilli) {
		Promise p = new Promise(UUID.randomUUID().toString(), new Date(System.currentTimeMillis() + durationInMilli));
		promiseTokens.add(p);
		return p.getToken();
	}

	/**
	 * @param promiseToken
	 * @throws ValidationException
	 */
	@Override
	public synchronized void validateToken(String promiseToken) throws ValidationException {
		for (Promise p : promiseTokens) {
			if (p.getToken().equals(promiseToken)) {
				return;
			}
		}
		throw new ValidationException("No such promise", null);
	}

	/**
	 * @param promiseToken
	 * @throws ValidationException
	 */
	@Override
	public synchronized void redeem(String promiseToken) throws ValidationException {
		Iterator<Promise> it = promiseTokens.iterator();
		while (it.hasNext()) {
			Promise p = it.next();
			if (p.token.equals(promiseToken)) {
				it.remove();
				return;
			}
		}
		throw new ValidationException("No such promise", null);
	}

	/**
	 * @param promiseToken
	 * @throws ValidationException
	 */
	@Override
	public void extendPromise(String promiseToken, long durationInMiliseconds) throws ValidationException {
		for (Promise p : promiseTokens) {
			if (p.getToken().equals(promiseToken)) {
				p.expiry = new Date(System.currentTimeMillis() + durationInMiliseconds);
				return;
			}
		}
		throw new ValidationException("No such promise", null);
	}

	@Override
	public boolean isNextPromise(String promiseToken) {
		cullExpiredPromisis();
		return !promiseTokens.isEmpty() && promiseTokens.peek().getToken().equals(promiseToken);
	}

	/**
	 * @return
	 */
	@Override
	public int getNumberOfOutstandingPromises() {
		cullExpiredPromisis();
		return promiseTokens.size();
	}

	// For testing
	public void clearAllPromises() {
		promiseTokens.clear();
	}

}
