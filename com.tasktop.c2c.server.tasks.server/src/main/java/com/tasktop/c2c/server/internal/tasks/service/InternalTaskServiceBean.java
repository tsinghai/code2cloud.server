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
package com.tasktop.c2c.server.internal.tasks.service;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

@Service("internalTaskService")
@Transactional
public class InternalTaskServiceBean extends AbstractJpaServiceBean implements InternalTaskService {

	@Override
	public Profile provisionAccount(AuthenticationToken authenticationToken) {
		String username = authenticationToken.getUsername();
		String displayName = authenticationToken.getFirstName() + ' ' + authenticationToken.getLastName();
		return doProvisionAccount(username, displayName);
	}

	private Profile doProvisionAccount(String username, String displayName) {
		Query query = entityManager.createQuery("select e from " + Profile.class.getSimpleName()
				+ " e where e.loginName = :username");
		query.setParameter("username", username);

		Profile profile;
		try {
			profile = (Profile) query.getSingleResult();
		} catch (NoResultException e) {
			profile = new Profile();
			profile.setLoginName(username);
			profile.setDisabledtext("");
		}
		profile.setRealname(displayName);
		if (!entityManager.contains(profile)) {
			entityManager.persist(profile);
			entityManager.flush(); // Make id available
		}
		return profile;
	}

	@Override
	public Profile provisionAccount(TaskUserProfile taskUserProfile) {
		Profile profile = doProvisionAccount(taskUserProfile.getLoginName(), taskUserProfile.getRealname());
		profile.setGravatarHash(taskUserProfile.getGravatarHash());
		return profile;
	}

	// We only want users calling this method if they are somehow authenticated - we don't want to create an account for
	// an anonymous user in the system.
	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public Profile getCurrentUserProfile() {
		AuthenticationServiceUser serviceUser = AuthenticationServiceUser.getCurrent();
		if (serviceUser == null) {
			return null;
		}
		AuthenticationToken token = serviceUser.getToken();
		return provisionAccount(token);
	}

	@Override
	public Profile findProfile(String username) throws EntityNotFoundException {
		Query query = entityManager.createQuery("select e from " + Profile.class.getSimpleName()
				+ " e where e.loginName = :username");
		query.setParameter("username", username);
		Profile profile;
		try {
			profile = (Profile) query.getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException("No Profile with loginName: " + username);
		}
		return profile;
	}

}
