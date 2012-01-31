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
package com.tasktop.c2c.server.profile.domain.internal;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tasktop.c2c.server.cloud.domain.ServiceType;

/**
 * A project service. Example data for a tasks service:
 * <dl>
 * <dt>uriPattern</dt>
 * <dd>'/tasks(/.*)'</dd>
 * <dt>internalPort</dt>
 * <dd>8080</dd>
 * <dt>internalUriPrefix</dt>
 * <dd>'/tasks/api'</dd>
 * </dl>
 * This would cause a request to '/tasks/task/2' (or, fully qualified
 * http://code2cloud.com/s/projectidentity/tasks/task/2) to match correctly and be mapped to '/tasks/api/task/2'. Note
 * that the capturing group is important in order to be able to append the relevant portion of the incoming URI to the
 * internalUriPrefix.
 * 
 * @author David Green
 */
@Entity
public class ProjectService extends BaseEntity {
	private ServiceType type;
	private Date allocationTime;
	private ProjectServiceProfile projectServiceProfile;
	private ServiceHost serviceHost;

	private String uriPattern;

	private String internalProtocol;
	private Integer internalPort;
	private Integer ajpPort;
	private String internalUriPrefix;

	/** For services that are not proxied, and can be accessed externally, EG app cloud. */
	private String externalUrl;

	@Enumerated(EnumType.STRING)
	public ServiceType getType() {
		return type;
	}

	public void setType(ServiceType type) {
		this.type = type;
	}

	@ManyToOne
	public ProjectServiceProfile getProjectServiceProfile() {
		return projectServiceProfile;
	}

	public void setProjectServiceProfile(ProjectServiceProfile projectServiceProfile) {
		this.projectServiceProfile = projectServiceProfile;
	}

	@ManyToOne(cascade = { CascadeType.PERSIST })
	public ServiceHost getServiceHost() {
		return serviceHost;
	}

	public void setServiceHost(ServiceHost serviceHost) {
		this.serviceHost = serviceHost;
	}

	/**
	 * the URI {@link java.util.regex.Pattern pattern} used to identify the service when accessed from outside of the
	 * cloud
	 */
	public String getUriPattern() {
		return uriPattern;
	}

	public void setUriPattern(String uriPattern) {
		this.uriPattern = uriPattern;
	}

	public void setInternalPort(Integer internalPort) {
		this.internalPort = internalPort;
	}

	public Integer getInternalPort() {
		return internalPort;
	}

	public Integer getAjpPort() {
		return ajpPort;
	}

	public void setAjpPort(Integer ajpPort) {
		this.ajpPort = ajpPort;
	}

	public String getInternalProtocol() {
		return internalProtocol;
	}

	public void setInternalProtocol(String internalProtocol) {
		this.internalProtocol = internalProtocol;
	}

	public String getInternalUriPrefix() {
		return internalUriPrefix;
	}

	public void setInternalUriPrefix(String internalUriPrefix) {
		this.internalUriPrefix = internalUriPrefix;
	}

	/**
	 * get the internal base URI to this service
	 * 
	 * @return the base URI, or null if the service host is not configured.
	 */
	@Transient
	public String getInternalBaseUri() {
		if (getServiceHost() == null || getServiceHost().getInternalNetworkAddress() == null) {
			return null;
		}
		String uri = internalProtocol == null ? "http" : internalProtocol;
		uri += "://";
		uri += getServiceHost().getInternalNetworkAddress();
		if (internalPort != null) {
			uri += ':';
			uri += internalPort;
		}
		if (internalUriPrefix != null) {
			uri += internalUriPrefix;
		}
		return uri;

	}

	/**
	 * get the internal base URI to this service
	 * 
	 * @return the base URI, or null if the service host is not configured.
	 */
	@Transient
	public String getInternalProxyBaseUri() {
		return computeInternalProxyBaseUri(true);

	}

	/**
	 * compute an internal base URI for this service
	 * 
	 * @param enableAjp
	 *            indicate if AJP should be considered as a protocol
	 * @return the internal URI, or null if it is not available.
	 * 
	 * @see #getInternalBaseUri()
	 */
	public String computeInternalProxyBaseUri(boolean enableAjp) {
		if (getServiceHost() == null || getServiceHost().getInternalNetworkAddress() == null) {
			return null;
		}
		String uri;
		if (enableAjp && getAjpPort() != null) {
			uri = "ajp://";
			uri += getServiceHost().getInternalNetworkAddress();
			uri += ':';
			uri += getAjpPort();
		} else {
			uri = internalProtocol == null ? "http" : internalProtocol;
			uri += "://";
			uri += getServiceHost().getInternalNetworkAddress();
			if (internalPort != null) {
				uri += ':';
				uri += internalPort;
			}
		}
		if (internalUriPrefix != null) {
			uri += internalUriPrefix;
		}
		return uri;
	}

	/**
	 * indicate if this project services matches the uri fragment according to its {@link #getUriPattern() uri pattern}.
	 * 
	 * @param uri
	 * @return
	 */
	public boolean matchesUri(String uri) {
		return uriPattern != null && Pattern.compile(uriPattern).matcher(uri).matches();
	}

	/**
	 * compute the internal proxy URI including protocol, host name, port, uri prefix.
	 * 
	 * @param uri
	 *            the {@link #matchesUri(String) matching} uri
	 * @return
	 */
	public String computeInternalProxyUri(String uri) {
		return computeInternalProxyUri(uri, true);
	}

	/**
	 * compute the internal proxy URI including protocol, host name, port, uri prefix.
	 * 
	 * @param uri
	 *            the {@link #matchesUri(String) matching} uri
	 * @param enableAjp
	 *            indicate if AJP should be enabled as a protocol
	 * @return the URI, or null if it is unavailable or does not match the given uri
	 */
	public String computeInternalProxyUri(String uri, boolean enableAjp) {
		Matcher matcher = Pattern.compile(uriPattern).matcher(uri);
		if (!matcher.matches()) {
			return null;
		}
		String base = computeInternalProxyBaseUri(enableAjp);
		if (base == null) {
			return null;
		}
		String suffix = uri;
		if (matcher.groupCount() > 0) {
			suffix = matcher.group(1);
		}
		if (base.endsWith("/") && suffix.startsWith("/")) {
			suffix = suffix.substring(1);
		}
		return base + suffix;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	public Date getAllocationTime() {
		return allocationTime;
	}

	public void setAllocationTime(Date allocationTime) {
		this.allocationTime = allocationTime;
	}
}
