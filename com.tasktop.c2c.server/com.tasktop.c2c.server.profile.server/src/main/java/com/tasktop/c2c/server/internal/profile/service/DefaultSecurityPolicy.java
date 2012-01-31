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
package com.tasktop.c2c.server.internal.profile.service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.internal.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.profile.domain.internal.ConfigurationProperty;
import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;

/**
 * implements data-level security policies
 * 
 * @author David Green
 */
@Service("securityPolicy")
@Transactional
public class DefaultSecurityPolicy implements SecurityPolicy, InitializingBean {

	private enum Operation {
		/**
		 * create the object
		 */
		CREATE,
		/**
		 * modify the object
		 */
		MODIFY,
		/**
		 * delete an object
		 */
		DELETE,
		/**
		 * add a related object to
		 */
		ADD,
		/**
		 * remove a related object from
		 */
		REMOVE,
		/**
		 * retrieve an object
		 */
		RETRIEVE
	}

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	protected InternalAuthenticationService internalAuthenticationService;

	private boolean enabled = true;

	@Override
	public void create(Object target) throws InsufficientPermissionsException {
		verify(Operation.CREATE, target, null, null);
	}

	@Override
	public void modify(Object target) throws InsufficientPermissionsException {
		verify(Operation.MODIFY, target, null, null);
	}

	@Override
	public void delete(Object target) throws InsufficientPermissionsException {
		verify(Operation.DELETE, target, null, null);
	}

	@Override
	public void retrieve(Object target) throws InsufficientPermissionsException {
		verify(Operation.RETRIEVE, target, null, null);
	}

	@Override
	public void add(Object parent, Object child, String path) throws InsufficientPermissionsException {
		verify(Operation.ADD, parent, child, path);
	}

	@Override
	public void remove(Object parent, Object child, String path) throws InsufficientPermissionsException {
		verify(Operation.REMOVE, parent, child, path);
	}

	public void verify(Operation operation, Object target, Object child, String qualifier)
			throws InsufficientPermissionsException {
		if (!enabled) {
			return;
		}
		// system role can do anything
		if (Security.hasRole(Role.System)) {
			return;
		}

		// IMPLEMENTATION NOTE:
		// this code is designed to fail by default. Only explicitly allowed
		// actions
		// should pass: therefore all conditions must fall through to the throws
		// clause
		// at the end, unless explicitly allowable.

		if (target instanceof Profile) {
			switch (operation) {
			case MODIFY:
				if (getCurrentUserProfile() != null && getCurrentUserProfile().getAdmin()) {
					return;
				}
				assertEquals(getCurrentUserProfile(), target); // FIXME

				return;
			case RETRIEVE:
				assertAuthenticated();
				return;
			case CREATE:
				return;
			}
		} else if (target instanceof Project) {
			Project targetProject = (Project) target;
			switch (operation) {
			case RETRIEVE:
				if (!targetProject.getPublic()) {
					assertMember(targetProject);
				}
				return;
			case MODIFY:
				assertOwner(targetProject);
				return;
			case CREATE:
				return;
			case ADD:
				if ("projectProfiles".equals(qualifier)) {
					assertOwner(targetProject);
					return;
				}
				break;

			case REMOVE:
				if ("projectProfiles".equals(qualifier)) {
					if (child != null && child.equals(getCurrentUserProfile())) {
						// a profile can always self-remove
						return;
					}
					assertOwner(targetProject);
					return;
				}
				break;
			}
		} else if (target instanceof ProjectProfile) {
			ProjectProfile projectLink = (ProjectProfile) target;
			switch (operation) {
			case RETRIEVE:
				assertMember(projectLink.getProject());
				return;
			case MODIFY:
				assertOwner(projectLink.getProject());
				return;
			}
		} else if (target instanceof InvitationToken) {
			InvitationToken token = (InvitationToken) target;
			switch (operation) {
			case CREATE:
				assertOwner(token.getProject());
				return;
			case MODIFY:
				return;
			}
		} else if (target instanceof ScmRepository) {
			ScmRepository repo = (ScmRepository) target;
			switch (operation) {
			case CREATE:
			case DELETE:
				assertOwner(repo.getProject());
				return;
			case RETRIEVE:
				if (!repo.getProject().getPublic()) {
					assertMember(repo.getProject());
				}
				return;
			}
		} else if (target instanceof ConfigurationProperty) {
			switch (operation) {
			// Only allow retrieve, but always allow it
			case RETRIEVE:
				return;
			}
		} else if (target instanceof DeploymentConfiguration) {
			DeploymentConfiguration deployment = (DeploymentConfiguration) target;
			switch (operation) {
			case CREATE:
			case DELETE:
			case MODIFY:
				assertMember(deployment.getProject());
				return;
			case RETRIEVE:
				if (!deployment.getProject().getPublic()) {
					assertMember(deployment.getProject());
				}
				return;
			}
		}
		// if we reach here then we're trying to do something for which there
		// is no policy. Deny by default.
		throw new InsufficientPermissionsException();
	}

	private void assertAuthenticated() {
		if (Security.getCurrentUser() == null) {
			throw new InsufficientPermissionsException();
		}
	}

	private Profile getCurrentUserProfile() {
		String currentUser = Security.getCurrentUser();

		if (currentUser != null) {
			try {
				return (Profile) entityManager
						.createQuery("select e from " + Profile.class.getSimpleName() + " e where e.username = :u")
						.setParameter("u", currentUser).getSingleResult();

			} catch (NoResultException e) {
				// expected
			}
		}
		return null;
	}

	private void assertMember(Project target) throws InsufficientPermissionsException {
		String userRole = internalAuthenticationService.toCompoundRole(Role.User, target.getIdentifier());

		if (Security.hasRole(userRole)) {
			// is a user
			return;
		}

		assertOwner(target); // Not a user, assert owner role
	}

	private void assertOwner(Project target) throws InsufficientPermissionsException {
		String adminRole = internalAuthenticationService.toCompoundRole(Role.Admin, target.getIdentifier());

		if (Security.hasRole(adminRole)) {
			// is an admin
			return;
		}
		throw new InsufficientPermissionsException();
	}

	private void assertEquals(Object a, Object b) throws InsufficientPermissionsException {
		if (a == b || (a != null && a.equals(b))) {
			return;
		}
		throw new InsufficientPermissionsException();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!enabled) {
			LoggerFactory.getLogger(SecurityPolicy.class.getName()).warn(
					"****** Data-level security is disabled! ******");
		}
	}

}
