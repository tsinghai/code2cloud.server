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
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.domain.internal.PromiseToken;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@Service("promiseService")
@Qualifier("main")
@Transactional(rollbackFor = { Exception.class })
public class PromiseServiceImpl extends AbstractJpaServiceBean implements PromiseService {

	@Override
	public int getNumberOfOutstandingPromises() {
		Date now = new Date();
		return ((Long) entityManager
				.createQuery(
						"SELECT COUNT(t) FROM " + PromiseToken.class.getSimpleName()
								+ " t WHERE t.expiryDate > :now AND t.dateUsed IS NULL").setParameter("now", now)
				.getSingleResult()).intValue();
	}

	@Override
	public boolean isNextPromise(String promiseToken) {
		Date now = new Date();
		List<PromiseToken> tokens = (List<PromiseToken>) entityManager
				.createQuery(
						"SELECT t FROM " + PromiseToken.class.getSimpleName()
								+ " t WHERE t.expiryDate > :now AND t.dateUsed IS NULL ORDER BY t.dateCreated ASC")
				.setParameter("now", now).setMaxResults(1).getResultList();
		if (tokens.isEmpty()) {
			return false;
		}
		return tokens.get(0).getToken().equals(promiseToken);
	}

	@Override
	public void extendPromise(String promiseToken, long durationInMiliseconds) throws ValidationException {
		Date newExiry = new Date(System.currentTimeMillis() + durationInMiliseconds);
		retrieve(promiseToken).setExpiryDate(newExiry);
	}

	private PromiseToken retrieve(String token) throws ValidationException {
		Date now = new Date();
		try {
			return (PromiseToken) entityManager
					.createQuery(
							"SELECT t FROM " + PromiseToken.class.getSimpleName()
									+ " t WHERE t.token = :token AND t.expiryDate > :now AND t.dateUsed IS NULL")
					.setParameter("now", now).setParameter("token", token).getSingleResult();
		} catch (NoResultException e) {
			throw new ValidationException("No such promise", null);
		}
	}

	@Override
	public void redeem(String promiseToken) throws ValidationException {
		if (!isNextPromise(promiseToken)) {
			throw new ValidationException("Not the next promise", null);
		}
		retrieve(promiseToken).setDateUsed(new Date());
		// REVIEW Or we could delete it?
	}

	@Override
	public void validateToken(String promiseToken) throws ValidationException {
		retrieve(promiseToken);
	}

	@Override
	public String getNewPromiseToken(long durationInMilli) {
		PromiseToken token = new PromiseToken();
		token.setDateCreated(new Date());
		token.setToken(UUID.randomUUID().toString());
		token.setExpiryDate(new Date(System.currentTimeMillis() + durationInMilli));
		entityManager.persist(token);
		return token.getToken();
	}

	@Override
	public boolean hasOutstandingPromises() {
		return getNumberOfOutstandingPromises() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.cloud.service.PromiseService#clearAllPromises()
	 */
	@Override
	public void clearAllPromises() {
		for (PromiseToken t : (List<PromiseToken>) entityManager.createQuery(
				"SELECT t FROM " + PromiseToken.class.getSimpleName() + " t").getResultList()) {
			entityManager.remove(t);
		}

	}

}
