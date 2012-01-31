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

import java.security.PublicKey;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.github.api.GitHub;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import com.tasktop.c2c.server.auth.service.AbstractAuthenticationServiceBean;
import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.InternalAuthenticationService;
import com.tasktop.c2c.server.auth.service.PublicKeyAuthenticationService;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.profile.domain.internal.Agreement;
import com.tasktop.c2c.server.profile.domain.internal.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;

@Qualifier("main")
@Service("authenticationService")
public class ProfileAuthenticationServiceBean extends AbstractAuthenticationServiceBean<Profile> implements
		UserDetailsService, SignInAdapter, AuthenticationServiceInternal, PublicKeyAuthenticationService {

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private InternalAuthenticationService internalAuthenticationService;

	private UsersConnectionRepository usersConnRepo;

	@Autowired
	@Override
	public void setUsersConnectionRepository(UsersConnectionRepository newConnRepo) {
		this.usersConnRepo = newConnRepo;
	}

	@Override
	public String signIn(String username, Connection<?> connection, NativeWebRequest request) {
		// Pull in this user's information
		AuthenticationServiceUser user = (AuthenticationServiceUser) loadUserByUsername(username);

		// Get a GitHub connection for this user.
		ConnectionRepository connRepo = this.usersConnRepo.createConnectionRepository(username);
		Connection<GitHub> apiConn = connRepo.findPrimaryConnection(GitHub.class);

		if (apiConn == null) {
			// No connection exists right now - that shouldn't happen since this is called from the GitHub connector
			// code. Something's fishy - throw an exception to terminate this call.
			throw new IllegalStateException("No GitHub connection exists for the given user!");
		}

		// Get our connection data so that we can populate the authentication token.
		ConnectionData connData = apiConn.createData();

		// Create a token for this user and push it into our security context, using the access token as the
		// credentials.
		AuthenticationToken token = user.getToken();
		AuthUtils.insertNewAuthToken(user, connData.getAccessToken(), token.getAuthorities(), token);
		return null;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		try {
			Profile profile = getProfileByUsername(username);
			AuthenticationToken token = createAuthenticationToken(username, profile);
			return AuthenticationServiceUser.fromAuthenticationToken(token, profile.getPassword());
		} catch (NoResultException e) {
			throw new UsernameNotFoundException(username);
		}
	}

	private Profile getProfileByUsername(String username) throws NoResultException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Profile> query = criteriaBuilder.createQuery(Profile.class);
		Root<Profile> root = query.from(Profile.class);
		query.select(root).where(criteriaBuilder.equal(root.get("username"), username));

		return entityManager.createQuery(query).getSingleResult();
	}

	@Override
	protected Profile validateCredentials(String username, String password) throws AuthenticationException {
		if (username == null || password == null) {
			// Bail out now.
			return null;
		}

		// First, try the password matcher.
		Profile retProfile = checkIfCredentialsMatchOnPassword(username, password);

		if (retProfile == null) {
			// No match? Try the GitHub key
			retProfile = checkIfCredentialsMatchOnGithubKey(username, password);
		}

		if (retProfile != null && retProfile.getDisabled() != null && retProfile.getDisabled()) {
			throw new AuthenticationException("Account disabled");
		}

		return retProfile;
	}

	private Profile checkIfCredentialsMatchOnPassword(String username, String password) {
		try {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Profile> query = criteriaBuilder.createQuery(Profile.class);
			Root<Profile> root = query.from(Profile.class);
			query.select(root)
					.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("username"), username),
							criteriaBuilder.equal(root.get("password"), passwordEncoder.encodePassword(password, null))));

			Profile profile = entityManager.createQuery(query).getSingleResult();
			return profile;
		} catch (NoResultException e) {
			// ignore, expected
			return null;
		}
	}

	private Profile checkIfCredentialsMatchOnGithubKey(String username, String githubKey) {
		// Get a connection for this user, if one exists.
		ConnectionRepository connRepo = this.usersConnRepo.createConnectionRepository(username);
		Connection<GitHub> apiConn = connRepo.findPrimaryConnection(GitHub.class);

		if (apiConn == null) {
			// No connection exists right now.
			return null;
		}

		// If there's a connection, check if the keys match.
		if (githubKey.equals(apiConn.createData().getAccessToken())) {
			// Keys match - return our profile now.
			try {
				return getProfileByUsername(username);
			} catch (NoResultException nre) {
				// The username didn't exist in the system - can't authenticate.
				return null;
			}
		}

		return null;
	}

	@Override
	protected void configureToken(Profile profile, AuthenticationToken token) {
		token.setFirstName(profile.getFirstName());
		token.setLastName(profile.getLastName());
	}

	private boolean hasPendingAgreements(Profile profile) {

		Query q = entityManager.createQuery(
				"select a from " + Agreement.class.getSimpleName() + " a where a.active = :active").setParameter(
				"active", true);

		List<Agreement> activeAgreements = q.getResultList();

		Query q2 = entityManager.createQuery(
				"select ap.agreement from " + AgreementProfile.class.getSimpleName()
						+ " ap where ap.profile = :profile").setParameter("profile", profile);

		List<Agreement> agreedAgreements = q2.getResultList();

		for (Agreement agreement : activeAgreements) {
			if (agreedAgreements.indexOf(agreement) == -1) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void addAuthorities(Profile profile, AuthenticationToken token) {
		// add our default roles now.
		super.addAuthorities(profile, token);
		if (profile.getDisabled()) {
			token.getAuthorities().clear();
			return;
		}

		if (hasPendingAgreements(profile)) {
			token.getAuthorities().remove(Role.User);
			token.getAuthorities().add(Role.UserWithPendingAgreements);
		}
		if (profile.getAdmin() == true) {
			token.getAuthorities().add(Role.Admin);
		}
		for (ProjectProfile projectProfile : profile.getProjectProfiles()) {
			String projectIdentifier = projectProfile.getProject().getIdentifier();

			// Add our appropriate roles now.
			if (projectProfile.getOwner()) {
				token.getAuthorities().add(internalAuthenticationService.toCompoundRole(Role.Admin, projectIdentifier));
			}

			if (projectProfile.getUser()) {
				token.getAuthorities().add(internalAuthenticationService.toCompoundRole(Role.User, projectIdentifier));
			}

			if (projectProfile.getCommunity()) {
				token.getAuthorities().add(
						internalAuthenticationService.toCompoundRole(Role.Community, projectIdentifier));
			}
		}
	}

	@Override
	public AuthenticationToken authenticate(String username, PublicKey publicKey) throws AuthenticationException {

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Profile> query = criteriaBuilder.createQuery(Profile.class);
		Root<Profile> root = query.from(Profile.class);
		query.select(root).where(criteriaBuilder.equal(root.get("username"), username));

		try {
			Profile profile = entityManager.createQuery(query).getSingleResult();

			for (SshPublicKey sshPublicKey : profile.getSshPublicKeys()) {
				if (sshPublicKey.isSameAs(publicKey)) {
					return createAuthenticationToken(username, profile);
				}
			}
			throw new AuthenticationException();
		} catch (NoResultException e) {
			throw new UsernameNotFoundException(username);
		}
	}
}
