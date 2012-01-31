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
package com.tasktop.c2c.server.common.service.web;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

public class AbstractTrustedHostRestServiceClient extends AbstractRestServiceClient implements InitializingBean {

	private class ConfiguringClientHttpRequestFactory implements ClientHttpRequestFactory {

		private final ClientHttpRequestFactory delegate;

		public ConfiguringClientHttpRequestFactory(ClientHttpRequestFactory delegate) {
			this.delegate = delegate;
		}

		public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
			ClientHttpRequest request = delegate.createRequest(uri, httpMethod);
			if (currentProjectIdentifier.get() != null) {
				request.getHeaders()
						.add(HeaderConstants.TRUSTED_HOST_PROJECT_ID_HEADER, currentProjectIdentifier.get());
			}
			return request;
		}
	}

	protected ThreadLocal<String> currentProjectIdentifier = new ThreadLocal<String>();

	public void afterPropertiesSet() throws Exception {
		this.template.setRequestFactory(new ConfiguringClientHttpRequestFactory(this.template.getRequestFactory()));
	}

}
