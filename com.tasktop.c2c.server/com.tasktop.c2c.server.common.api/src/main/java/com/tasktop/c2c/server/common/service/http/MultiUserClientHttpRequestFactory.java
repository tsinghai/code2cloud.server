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
package com.tasktop.c2c.server.common.service.http;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tasktop.c2c.server.common.service.Security;

/**
 * a client request factory that can handle multithreaded access by different users.
 */
@SuppressWarnings("serial")
public class MultiUserClientHttpRequestFactory implements ClientHttpRequestFactory, DisposableBean {

	private static final String PARAM_AUTH_KEY = MultiUserClientHttpRequestFactory.class.getName() + "#auth";

	protected int threshold = 1000;

	private LinkedHashMap<String, CommonsClientHttpRequestFactory> requestFactoryByUser = new LinkedHashMap<String, CommonsClientHttpRequestFactory>() {
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<String, CommonsClientHttpRequestFactory> eldest) {
			if (size() > threshold) {
				dispose(eldest.getValue());
				return true;
			}
			return false;
		}
	};

	private HttpConnectionManager connectionManager;

	private CommonsClientHttpRequestFactorySource clientHttpRequestFactorySource = new CommonsClientHttpRequestFactorySource() {
		public CommonsClientHttpRequestFactory newInstance(HttpClient httpClient) {
			return new CommonsClientHttpRequestFactory(httpClient) {
				@Override
				public void destroy() {
					// nothing to do: the connection manager is shared.
				}
			};
		}
	};

	private String cookiePolicy;
	boolean authenticationPreemptive = true;

	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
		String user = Security.getCurrentUser();
		if (user == null) {
			user = "";
		}
		CommonsClientHttpRequestFactory requestFactory = computeRequestFactory(user);
		synchronized (requestFactory) {
			if (user.length() > 0) {
				updateCredentials(requestFactory, user);
			}
			return requestFactory.createRequest(uri, httpMethod);
		}
	}

	/**
	 * public for testing purposes only
	 */
	public final CommonsClientHttpRequestFactory computeRequestFactory(String user) {
		CommonsClientHttpRequestFactory requestFactory;
		synchronized (requestFactoryByUser) {
			requestFactory = requestFactoryByUser.get(user);
			if (requestFactory == null) {
				requestFactory = createClientHttpRequestFactory();
				requestFactoryByUser.put(user, requestFactory);
			}
		}
		return requestFactory;
	}

	private CommonsClientHttpRequestFactory createClientHttpRequestFactory() {
		HttpClient httpClient = new HttpClient(connectionManager);
		return clientHttpRequestFactorySource.newInstance(httpClient);
	}

	protected void dispose(ClientHttpRequestFactory factory) {
		if (factory instanceof DisposableBean) {
			try {
				((DisposableBean) factory).destroy();
			} catch (Exception e) {
				LoggerFactory.getLogger(MultiUserClientHttpRequestFactory.class).error(e.getMessage(), e);
			}
		}
	}

	protected void updateCredentials(CommonsClientHttpRequestFactory requestFactory, String user) {
		HttpClient httpClient = requestFactory.getHttpClient();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			setCredentials(httpClient, authentication);
		} else {
			clearAuthState(httpClient);
			LoggerFactory.getLogger(MultiUserClientHttpRequestFactory.class.getName()).warn("Anonymous request");
		}
	}

	protected void setCredentials(HttpClient httpClient, Authentication authentication) {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		String username = token.getName();
		String password = token.getCredentials() == null ? "" : token.getCredentials().toString();

		// FIXME: review, should probably not be AuthScope.ANY
		Credentials credentials = new UsernamePasswordCredentials(username, password);
		Object auth = httpClient.getParams().getParameter(PARAM_AUTH_KEY);
		if (auth == null || !auth.equals(credentials)) {
			clearAuthState(httpClient);

			httpClient.getParams().setParameter(PARAM_AUTH_KEY, credentials);
			httpClient.getState().setCredentials(AuthScope.ANY, credentials);
		}

		// this seems to be necessary for correct operation.
		// it seems that with keepalives a response to an auth challenge is issued
		// without first reading the whole response, making the 2nd request read the wrong
		// data for the HTTP status line.
		httpClient.getParams().setAuthenticationPreemptive(authenticationPreemptive);
		if (cookiePolicy != null) {
			httpClient.getParams().setCookiePolicy(cookiePolicy);
		}
	}

	protected void clearAuthState(HttpClient httpClient) {
		// bug 180: clear any cookies if credentials have changed. This ensures that the previous session is
		// not reused, thus causing the server to validate the credentials again.
		httpClient.getState().clearCookies();
		httpClient.getState().clearCredentials();
	}

	/**
	 * the threshold for the number of concurrent users that should be remembered in the cache of client state
	 */
	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		if (threshold < 1) {
			throw new IllegalArgumentException();
		}
		this.threshold = threshold;
	}

	public HttpConnectionManager getConnectionManager() {
		return connectionManager;
	}

	@Required
	public void setConnectionManager(HttpConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public String getCookiePolicy() {
		return cookiePolicy;
	}

	public void setCookiePolicy(String cookiePolicy) {
		this.cookiePolicy = cookiePolicy;
	}

	public boolean isAuthenticationPreemptive() {
		return authenticationPreemptive;
	}

	public void setAuthenticationPreemptive(boolean authenticationPreemptive) {
		this.authenticationPreemptive = authenticationPreemptive;
	}

	public CommonsClientHttpRequestFactorySource getClientHttpRequestFactorySource() {
		return clientHttpRequestFactorySource;
	}

	public void setClientHttpRequestFactorySource(CommonsClientHttpRequestFactorySource clientHttpRequestFactorySource) {
		this.clientHttpRequestFactorySource = clientHttpRequestFactorySource;
	}

	public void destroy() throws Exception {
		synchronized (requestFactoryByUser) {
			for (ClientHttpRequestFactory entry : requestFactoryByUser.values()) {
				dispose(entry);
			}
			requestFactoryByUser.clear();
		}
		if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
			((MultiThreadedHttpConnectionManager) connectionManager).shutdown();
			connectionManager = null;
		}
	}

}
