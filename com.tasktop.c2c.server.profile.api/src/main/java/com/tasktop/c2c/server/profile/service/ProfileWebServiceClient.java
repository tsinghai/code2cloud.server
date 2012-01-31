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
package com.tasktop.c2c.server.profile.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;

@Service
@Qualifier("webservice-client")
public class ProfileWebServiceClient extends AbstractRestServiceClient implements ProfileWebService {

	@SuppressWarnings("unused")
	private static class ServiceCallResult {
		private Long projectId;
		private Long profileId;
		private Profile profile;
		private Boolean isWatching;
		private String token;
		private List<Project> projectList;
		private List<Agreement> agreements;
		private List<AgreementProfile> agreementProfiles;
		private Project project;
		private String[] roles;
		private QueryResult<Project> queryResult;
		private SignUpToken signUpToken;
		private List<SignUpToken> signUpTokens;
		private ProjectInvitationToken projectInvitationToken;

		private SshPublicKey sshPublicKey;
		private List<SshPublicKey> sshPublicKeyList;

		public void setProfile(Profile profile) {
			this.profile = profile;
		}

		public Profile getProfile() {
			return profile;
		}

		public List<Project> getProjectList() {
			return projectList;
		}

		public void setProjectList(List<Project> projectList) {
			this.projectList = projectList;
		}

		public void setAgreementList(List<Agreement> agreements) {
			this.agreements = agreements;
		}

		public List<Agreement> getAgreementList() {
			return agreements;
		}

		public void setAgreementProfileList(List<AgreementProfile> agreementProfiles) {
			this.agreementProfiles = agreementProfiles;
		}

		public List<AgreementProfile> getAgreementProfileList() {
			return agreementProfiles;
		}

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.project = project;
		}

		public String[] getRoles() {
			return roles;
		}

		public void setRoles(String[] roles) {
			this.roles = roles;
		}

		public QueryResult<Project> getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(QueryResult<Project> queryResult) {
			this.queryResult = queryResult;
		}

		public SignUpToken getSignUpToken() {
			return signUpToken;
		}

		public void setSignUpToken(SignUpToken signUpToken) {
			this.signUpToken = signUpToken;
		}

		public List<SignUpToken> getSignUpTokenList() {
			return signUpTokens;
		}

		public void setSignUpTokenList(List<SignUpToken> signUpTokens) {
			this.signUpTokens = signUpTokens;
		}

		public ProjectInvitationToken getProjectInvitationToken() {
			return projectInvitationToken;
		}

		public void setProjectInvitationToken(ProjectInvitationToken projectInvitationToken) {
			this.projectInvitationToken = projectInvitationToken;
		}

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		public Boolean getIsWatching() {
			return isWatching;
		}

		public void setIsWatching(Boolean isWatching) {
			this.isWatching = isWatching;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public Long getProfileId() {
			return profileId;
		}

		public void setProfileId(Long profileId) {
			this.profileId = profileId;
		}

		public SshPublicKey getSshPublicKey() {
			return sshPublicKey;
		}

		public void setSshPublicKey(SshPublicKey sshPublicKey) {
			this.sshPublicKey = sshPublicKey;
		}

		public List<SshPublicKey> getSshPublicKeyList() {
			return sshPublicKeyList;
		}

		public void setSshPublicKeyList(List<SshPublicKey> sshPublicKeyList) {
			this.sshPublicKeyList = sshPublicKeyList;
		}
	}

	private abstract class GetCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object... variables) {
			ServiceCallResult callResult = template.getForObject(computeUrl(urlStub), ServiceCallResult.class,
					variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to profileWebService");
			}

			return retVal;
		}
	}

	private abstract class PostCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object objToPost, Object... variables) {
			ServiceCallResult callResult = template.postForObject(computeUrl(urlStub), objToPost,
					ServiceCallResult.class, variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to profileWebService");
			}

			return retVal;
		}
	}

	// This ended up being much more involved than expected, due to constraints within the Spring RestTemplate - calling
	// template.delete() did not set an Accept: header, which meant that any exceptions which came back would cause
	// errors in the Spring filter pipeline and never arrive at the client. In order to set the Accept: content header,
	// it was necessary to mimic what RestTemplate does with GET and POST calls - that is why the below code was copied
	// from RestTemplate's getForObject() method and modified for our purposes.
	private class DeleteCall {

		public void doCall(String urlStub, Object... variables) {
			AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(ServiceCallResult.class);
			HttpMessageConverterExtractor<ServiceCallResult> responseExtractor = new HttpMessageConverterExtractor<ServiceCallResult>(
					ServiceCallResult.class, template.getMessageConverters());
			template.execute(computeUrl(urlStub), HttpMethod.DELETE, requestCallback, responseExtractor, variables);
		}
	}

	// This class is copied from Spring RestTemplate, since the source class is private and thus inaccessible to this
	// code otherwise.
	private class AcceptHeaderRequestCallback implements RequestCallback {

		private final Class<?> responseType;

		private AcceptHeaderRequestCallback(Class<?> responseType) {
			this.responseType = responseType;
		}

		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (responseType != null) {
				List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
				for (HttpMessageConverter<?> messageConverter : template.getMessageConverters()) {
					if (messageConverter.canRead(responseType, null)) {
						List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
						for (MediaType supportedMediaType : supportedMediaTypes) {
							if (supportedMediaType.getCharSet() != null) {
								supportedMediaType = new MediaType(supportedMediaType.getType(),
										supportedMediaType.getSubtype());
							}
							allSupportedMediaTypes.add(supportedMediaType);
						}
					}
				}
				if (!allSupportedMediaTypes.isEmpty()) {
					MediaType.sortBySpecificity(allSupportedMediaTypes);
					request.getHeaders().setAccept(allSupportedMediaTypes);
				}
			}
		}
	}

	public static final String GET_PROFILE_URL = "profile";

	public Profile getCurrentProfile() {
		return new GetCall<Profile>() {
			public Profile getValue(ServiceCallResult result) {
				return result.getProfile();
			}
		}.doCall(GET_PROFILE_URL);
	}

	public static final String UPDATE_PROFILE_URL = "profile";

	public void updateProfile(Profile profile) throws ValidationException, EntityNotFoundException {
		// FIXME this cannot be templated as usual because it returns void rather than a ServiceCallResult
		try {
			template.postForObject(computeUrl(UPDATE_PROFILE_URL), profile, Void.class);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			convertValidationException(e);
			throw (e);
		}
	}

	public static final String PROFILE_ID_URLPARAM = "profileId";

	public static final String GET_PROJECTS_URL = "profile/{" + PROFILE_ID_URLPARAM + "}/projects";

	public List<Project> getProjects(Long profileId) throws EntityNotFoundException {
		try {
			return new GetCall<List<Project>>() {
				public List<Project> getValue(ServiceCallResult result) {
					return result.getProjectList();
				}
			}.doCall(GET_PROJECTS_URL, String.valueOf(profileId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String CREATE_PROJECT_URL = "profile/{" + PROFILE_ID_URLPARAM + "}/project";

	public Project createProject(Long profileId, Project project) throws EntityNotFoundException, ValidationException {
		try {
			return new PostCall<Project>() {
				public Project getValue(ServiceCallResult result) {
					return result.getProject();
				}
			}.doCall(CREATE_PROJECT_URL, project, String.valueOf(profileId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			convertValidationException(e);
			throw e;
		}
	}

	@Autowired()
	@Qualifier("basicAuth")
	public void setRestTemplate(RestTemplate template) {
		super.setRestTemplate(template);
	}

	public static final String GET_PENDING_AGREEMENTS_URL = "agreements/pending";

	public List<Agreement> getPendingAgreements() throws EntityNotFoundException {
		try {
			return new GetCall<List<Agreement>>() {
				public List<Agreement> getValue(ServiceCallResult result) {
					return result.getAgreementList();
				}
			}.doCall(GET_PENDING_AGREEMENTS_URL);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String AGREEMENT_ID_URLPARAM = "agreementId";

	public static final String APPROVE_AGREEMENT_URL = "agreements/{" + AGREEMENT_ID_URLPARAM + "}/approve";

	public void approveAgreement(Long agreementId) throws EntityNotFoundException {
		// FIXME this cannot be templated as usual because it returns a Void rather than a ServiceCallResult
		try {
			template.postForObject(computeUrl(APPROVE_AGREEMENT_URL), null, Void.class, String.valueOf(agreementId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String GET_APPROVED_AGREEMENTS_URL = "agreements/approved";

	public List<AgreementProfile> getApprovedAgreementProfiles() throws EntityNotFoundException {
		try {
			return new GetCall<List<AgreementProfile>>() {
				public List<AgreementProfile> getValue(ServiceCallResult result) {
					return result.getAgreementProfileList();
				}
			}.doCall(GET_APPROVED_AGREEMENTS_URL);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String TOKEN_URLPARAM = "token";

	public static final String GET_PROJECT_FOR_INVITATION_URL = "invitation/{" + TOKEN_URLPARAM + "}/project";

	public Project getProjectForInvitationToken(String token) throws EntityNotFoundException {
		try {
			return new GetCall<Project>() {
				public Project getValue(ServiceCallResult result) {
					return result.getProject();
				}
			}.doCall(GET_PROJECT_FOR_INVITATION_URL, token);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String PROJECT_IDENTIFIER_URLPARAM = "projectIdentifier";

	public static final String GET_PROJECT_BY_IDENTIFIER_URL = "projects/{" + PROJECT_IDENTIFIER_URLPARAM + "}";

	public Project getProjectByIdentifier(String projectIdentifier) throws EntityNotFoundException {
		try {
			return new GetCall<Project>() {
				public Project getValue(ServiceCallResult result) {
					return result.getProject();
				}
			}.doCall(GET_PROJECT_BY_IDENTIFIER_URL, projectIdentifier);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String GET_ROLES_FOR_PROJECT_URL = "projects/{" + PROJECT_IDENTIFIER_URLPARAM + "}/roles";

	public String[] getRolesForProject(String projectIdentifier) throws EntityNotFoundException {
		try {
			return new GetCall<String[]>() {
				public String[] getValue(ServiceCallResult result) {
					return result.getRoles();
				}
			}.doCall(GET_ROLES_FOR_PROJECT_URL, projectIdentifier);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String UPDATE_PROJECT_URL = "projects/{" + PROJECT_IDENTIFIER_URLPARAM + "}";

	public Project updateProject(Project project) throws EntityNotFoundException, ValidationException {
		try {
			return new PostCall<Project>() {
				public Project getValue(ServiceCallResult result) {
					return result.getProject();
				}
			}.doCall(UPDATE_PROJECT_URL, project, project.getIdentifier());
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String QUERY_URLPARAM = "query";

	public static final String FIND_PROJECTS_URL = "search/{" + QUERY_URLPARAM + "}";

	public QueryResult<Project> findProjects(String query, QueryRequest request) {
		return new GetCall<QueryResult<Project>>() {
			public QueryResult<Project> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall(FIND_PROJECTS_URL, query);
	}

	public static final String PROJECT_WATCH_URL = "projects/{" + PROJECT_IDENTIFIER_URLPARAM + "}/watch";

	public void watchProject(String projectIdentifier) throws EntityNotFoundException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(PROJECT_IDENTIFIER_URLPARAM, projectIdentifier);

		try {
			template.postForEntity(computeUrl(PROJECT_WATCH_URL), null, Void.class, vars);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}
	}

	public Boolean isWatchingProject(String projectIdentifier) throws EntityNotFoundException {
		try {
			return new GetCall<Boolean>() {
				public Boolean getValue(ServiceCallResult result) {
					return result.getIsWatching();
				}
			}.doCall(PROJECT_WATCH_URL, projectIdentifier);

		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}
	}

	public static final String PROJECT_UNWATCH_URL = "projects/{" + PROJECT_IDENTIFIER_URLPARAM + "}/unwatch";

	public void unwatchProject(String projectIdentifier) throws EntityNotFoundException {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(PROJECT_IDENTIFIER_URLPARAM, projectIdentifier);

		try {
			template.postForEntity(computeUrl(PROJECT_UNWATCH_URL), null, Void.class, vars);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}
	}

	public static final String CREATE_SIGNUP_TOKEN_URL = "signup_token/";

	public SignUpToken createSignUpToken(SignUpToken token) throws ValidationException {
		try {
			return new PostCall<SignUpToken>() {
				public SignUpToken getValue(ServiceCallResult result) {
					return result.getSignUpToken();
				}
			}.doCall(CREATE_SIGNUP_TOKEN_URL, token);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw (e);
		}
	}

	public static final String GET_UNUSED_SIGNUP_TOKENS_URL = "signup_tokens/";

	public List<SignUpToken> getUnusedSignUpTokens() {
		try {
			return new GetCall<List<SignUpToken>>() {
				public List<SignUpToken> getValue(ServiceCallResult result) {
					return result.getSignUpTokenList();
				}
			}.doCall(GET_UNUSED_SIGNUP_TOKENS_URL);
		} catch (WrappedCheckedException e) {
			throw (e);
		}
	}

	public static final String EMAIL_URLPARAM = "email";

	public static final String SEND_SIGNUP_INVITATION_URL = "signup_invitation/{" + EMAIL_URLPARAM + "}";

	public void sendSignUpInvitation(String email) throws EntityNotFoundException {
		// FIXME this cannot be templated as usual because it returns void rather than a ServiceCallResult
		try {
			template.postForLocation(computeUrl(SEND_SIGNUP_INVITATION_URL), null, email);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}
	}

	public static final String GET_SIGNUP_TOKEN_URL = "signup/{" + TOKEN_URLPARAM + "}";

	public SignUpToken getSignUpToken(String token) {
		try {
			return new GetCall<SignUpToken>() {
				public SignUpToken getValue(ServiceCallResult result) {
					return result.getSignUpToken();
				}
			}.doCall(GET_SIGNUP_TOKEN_URL, token);
		} catch (WrappedCheckedException e) {
			throw (e);
		}
	}

	public static final String CREATE_PROFILE_WITH_SIGNUP_TOKEN_URL = "signup/{" + TOKEN_URLPARAM + "}";

	public Long createProfileWithSignUpToken(Profile profile, String token) throws ValidationException {
		try {
			return new PostCall<Long>() {
				public Long getValue(ServiceCallResult result) {
					return result.getProfileId();
				}
			}.doCall(CREATE_PROFILE_WITH_SIGNUP_TOKEN_URL, profile, token);

		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw (e);
		}
	}

	public static final String CREATE_PROFILE_URL = "signup";

	public Long createProfile(Profile profile) throws ValidationException {
		try {
			return new PostCall<Long>() {
				public Long getValue(ServiceCallResult result) {
					return result.getProfileId();
				}
			}.doCall(CREATE_PROFILE_URL, profile);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw (e);
		}
	}

	public static final String USER_EMAIL_PARAM = "userEmail";
	public static final String INVITE_USER_TO_PROJECT_URL = "project/{" + PROJECT_IDENTIFIER_URLPARAM + "}/invite/{"
			+ USER_EMAIL_PARAM + "}";

	public String inviteUserForProject(String email, String projectIdentifier) throws EntityNotFoundException {
		try {
			return new GetCall<String>() {
				public String getValue(ServiceCallResult result) {
					return result.getToken();
				}
			}.doCall(INVITE_USER_TO_PROJECT_URL, projectIdentifier, email);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}
	}

	public static final String ACCEPT_INVITE_URL = "accept/{" + TOKEN_URLPARAM + "}";

	public void acceptInvitation(String invitationToken) throws EntityNotFoundException {
		try {
			template.getForObject(computeUrl(ACCEPT_INVITE_URL), Void.class, invitationToken);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw (e);
		}

	}

	public static final String GET_PROJECT_INVITATION_TOKEN_URL = "invitation/{" + TOKEN_URLPARAM + "}";

	public ProjectInvitationToken getProjectInvitationToken(String token) throws EntityNotFoundException {
		try {
			return new GetCall<ProjectInvitationToken>() {
				public ProjectInvitationToken getValue(ServiceCallResult result) {
					return result.getProjectInvitationToken();
				}
			}.doCall(GET_PROJECT_INVITATION_TOKEN_URL, token);
		} catch (WrappedCheckedException e) {
			throw (e);
		}
	}

	public static final String CREATE_SSH_PUBLIC_KEY_URL = "ssh-key";

	public SshPublicKey createSshPublicKey(SshPublicKeySpec publicKey) throws ValidationException {
		try {
			return new PostCall<SshPublicKey>() {
				public SshPublicKey getValue(ServiceCallResult result) {
					return result.getSshPublicKey();
				}
			}.doCall(CREATE_SSH_PUBLIC_KEY_URL, publicKey);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw (e);
		}
	}

	public static final String LIST_SSH_PUBLIC_KEYS_URL = "ssh-keys";

	public List<SshPublicKey> listSshPublicKeys() {
		try {
			return new GetCall<List<SshPublicKey>>() {
				public List<SshPublicKey> getValue(ServiceCallResult result) {
					return result.getSshPublicKeyList();
				}
			}.doCall(LIST_SSH_PUBLIC_KEYS_URL);
		} catch (WrappedCheckedException e) {
			throw (e);
		}
	}

	public static final String SSH_KEY_ID_PARAM = "keyId";

	public static final String DELETE_SSH_PUBLIC_KEYS = "ssh-key/{" + SSH_KEY_ID_PARAM + "}";

	public void removeSshPublicKey(Long publicKeyId) throws EntityNotFoundException {
		try {
			new DeleteCall().doCall(DELETE_SSH_PUBLIC_KEYS, String.valueOf(publicKeyId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public QueryResult<Project> findProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest) {
		throw new UnsupportedOperationException();
	}

	public Boolean isProjectCreateAvailble() {
		throw new UnsupportedOperationException();
	}

	public Boolean isPasswordResetTokenAvailable(String token) {
		throw new UnsupportedOperationException();
	}

}
