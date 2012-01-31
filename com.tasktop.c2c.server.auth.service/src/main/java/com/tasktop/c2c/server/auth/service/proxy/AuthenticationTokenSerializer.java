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
package com.tasktop.c2c.server.auth.service.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.codec.Base64;

import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.web.HeaderConstants;


public class AuthenticationTokenSerializer {

	private final Charset charset;

	public AuthenticationTokenSerializer() {
		charset = Charset.forName("utf-8");
	}

	public void serialize(RequestHeaders headers, AuthenticationToken token) {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(1024);

		String data;
		try {
			GZIPOutputStream gzOut = new GZIPOutputStream(bytesOut);
			ObjectOutputStream out = new ObjectOutputStream(gzOut);
			out.writeObject(token);
			out.close();
			gzOut.close();
			data = new String(Base64.encode(bytesOut.toByteArray()), charset);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		headers.addHeader(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER,
				HeaderConstants.PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX + data);
	}

	public AuthenticationToken deserialize(HttpServletRequest request) {
		@SuppressWarnings("rawtypes")
		Enumeration headers = request.getHeaders(HeaderConstants.PREAUTH_AUTHORIZATION_HEADER);
		while (headers.hasMoreElements()) {
			String authorizationValue = headers.nextElement().toString();
			if (authorizationValue.startsWith(HeaderConstants.PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX)) {
				String value = authorizationValue.substring(
						HeaderConstants.PRE_AUTH_AUTHORIZATION_HEADER_VALUE_PREFIX.length()).trim();
				byte[] bytes;
				try {
					bytes = Base64.decode(value.getBytes(charset));
				} catch (IllegalArgumentException e) {
					// unexpected chars in base64
					continue;
				}
				try {
					ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
					try {
						Object token = in.readObject();
						return (AuthenticationToken) token;
					} finally {
						in.close();
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return null;
	}
}
