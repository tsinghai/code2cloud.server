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
package com.tasktop.c2c.server.common.web.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.web.shared.AuthenticationRequiredException;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;

@SuppressWarnings("serial")
public abstract class AbstractAutowiredRemoteServiceServlet extends RemoteServiceServlet {
	// IMPLEMENTATION NOTE: we can't rely on init() for initialization, since init() may be called without
	// a wrapping servlet filter to provide request context.
	private ServiceStrategy serviceStrategy = new InitializingServiceStrategy();

	public AbstractAutowiredRemoteServiceServlet() {
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		serviceStrategy.service(request, response);
	}

	private interface ServiceStrategy {
		public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException;
	}

	@Override
	public String processCall(String payload) throws SerializationException {
		try {
			RPCRequest rpcRequest = RPC.decodeRequest(payload, this.getClass(), this);
			onAfterRequestDeserialized(rpcRequest);
			return invokeAndEncodeResponse(rpcRequest.getMethod(), rpcRequest.getParameters(),
					rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
		} catch (IncompatibleRemoteServiceException ex) {
			log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
			return RPC.encodeResponseForFailure(null, ex);
		}
	}

	private String invokeAndEncodeResponse(Method serviceMethod, Object[] args,
			SerializationPolicy serializationPolicy, int flags) throws SerializationException {
		if (serviceMethod == null) {
			throw new NullPointerException("serviceMethod");
		}

		if (serializationPolicy == null) {
			throw new NullPointerException("serializationPolicy");
		}

		String responsePayload;
		try {
			Object result = serviceMethod.invoke(this, args);
			responsePayload = RPC.encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags);
		} catch (IllegalAccessException e) {
			SecurityException securityException = new SecurityException("Cannot access " + serviceMethod.getName());
			securityException.initCause(e);
			throw securityException;
		} catch (IllegalArgumentException e) {
			SecurityException securityException = new SecurityException("Cannot access " + serviceMethod.getName());
			securityException.initCause(e);
			throw securityException;
		} catch (InvocationTargetException e) {
			// Try to encode the caught exception
			//
			Throwable cause = e.getCause();
			cause = convertException(cause);
			responsePayload = RPC.encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags);
		}

		return responsePayload;
	}

	/**
	 * Convert exceptions to an alternative that will be recognized by the client application
	 */
	protected Throwable convertException(Throwable cause) {
		if ((cause instanceof AuthenticationCredentialsNotFoundException)
				|| (cause instanceof InsufficientAuthenticationException)) {
			cause = new AuthenticationRequiredException();
		}
		return cause;
	}

	private class InitializingServiceStrategy implements ServiceStrategy {

		@Override
		public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException {
			synchronized (this) {
				if (serviceStrategy == this) {
					Object beanToInitialize = AbstractAutowiredRemoteServiceServlet.this;
					try {
						WebApplicationContext context = WebApplicationContextUtils
								.getRequiredWebApplicationContext(getServletContext());
						context.getAutowireCapableBeanFactory().autowireBean(beanToInitialize);
					} catch (Throwable t) {
						String message = "Cannot autowire " + beanToInitialize.getClass().getName() + ": "
								+ t.getMessage();
						LoggerFactory.getLogger(beanToInitialize.getClass().getName()).error(message, t);
						getServletContext().log(message, t);
						if (t instanceof RuntimeException) {
							throw (RuntimeException) t;
						}
						throw new IllegalStateException(t);
					} finally {
						serviceStrategy = new DefaultServiceStrategy();
					}
				}
			}
			serviceStrategy.service(request, response);
		}

	}

	private class DefaultServiceStrategy implements ServiceStrategy {
		@Override
		public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException {
			doService(request, response);
		}
	}

	private void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		super.service(request, response);
	}

	@Autowired
	private MessageSource messageSource;

	private Pattern validationPattern = Pattern.compile("\\[(\\p{Alnum}+)|(\\-?\\p{Digit}+)\\]");

	protected void handle(ValidationException exception) throws ValidationFailedException {
		List<String> messages = new ArrayList<String>();
		if (exception.getErrors() != null) {
			for (ObjectError error : exception.getErrors().getAllErrors()) {
				try {
					// Check to see if this is one of our custom messages, intended for a multi-object form - if it is,
					// chop it up.
					Matcher matcher = validationPattern.matcher(error.getCode());
					String origErrorMsg = messageSource.getMessage(error, getLocale());

					if (matcher.find()) {
						// This is one of our custom messages - try message lookup with the custom section removed
						String newCode = error.getCode().substring(0, matcher.start());

						// This will return a new error message if it's present, or the original if it wasn't.
						String libraryErrorMessage = messageSource.getMessage(newCode, error.getArguments(),
								origErrorMsg, getLocale());

						// Grab our first match, and then issue another find() call to grab the second group.
						String className = matcher.group(1);

						String classId = null;
						if (matcher.find()) {
							classId = matcher.group(2);
						}

						// Add in this augmented message to our message list, so that it can be detected and handled by
						// our controls.
						messages.add(String.format("%s|%s|%s|%s", libraryErrorMessage, newCode, className, classId));
					} else {
						// It's not, so just do the normal processing.
						messages.add(origErrorMsg);
					}

				} catch (NoSuchMessageException t) {

					messages.add(t.getMessage());
					// FIXME: development only
					// throw t;
				}
			}
		} else {
			// This is the case for exceptions coming over the rest client.
			String[] messageSplits = exception.getMessage().split(",");
			messages.addAll(Arrays.asList(messageSplits));
		}
		throw new ValidationFailedException(messages);
	}

	// TODO : handle this correctly
	protected void handle(ConcurrentUpdateException exception) throws ValidationFailedException {
		throw new ValidationFailedException(Arrays.asList("The object has been modified since it was loaded"));
	}

	protected void handle(EntityNotFoundException e) throws NoSuchEntityException {
		throw new NoSuchEntityException();
	}

	private Locale getLocale() {
		// FIXME get user's locale
		return Locale.ENGLISH;
	}

	// REVIEW ugly that we have to do this in so many methods..
	protected void setTenancyContext(String projectIdentifier) {
		TenancyContextHolder.createEmptyContext();
		DefaultTenant tenant = new DefaultTenant();
		tenant.setIdentity(projectIdentifier);
		TenancyContextHolder.getContext().setTenant(tenant);
	}

}
