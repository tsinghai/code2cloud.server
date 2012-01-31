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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Conventions;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.tasktop.c2c.server.common.service.HttpStatusCodeException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.web.Error;

/**
 * A servlet filter that manages errors during an http request. Two main parts:
 * 
 * Renders the view based on a view resolver and the {@link Error} model. This enables proper response handling for REST
 * services.
 * 
 * Logs uncaught exceptions and sends an internal server error response.
 * 
 * @author David Green
 * @author Clint Morgan
 */
public class MessageErrorHandlerFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(MessageErrorHandlerFilter.class);

	@Autowired
	protected ContentNegotiatingViewResolver viewResolver;
	@Autowired
	protected LocaleResolver localeResolver;

	private List<MediaType> modelMediaTypes = new ArrayList<MediaType>();
	{
		modelMediaTypes.add(MediaType.APPLICATION_JSON);
		modelMediaTypes.add(MediaType.APPLICATION_XML);
	}

	private ModelAndView createModelAndView(String viewName, Object value) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		String variableName = Conventions.getVariableName(value);
		modelAndView.addObject(variableName, value);
		return modelAndView;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		MessageResponse messageResponse = new MessageResponse((HttpServletRequest) request,
				(HttpServletResponse) response);
		try {
			chain.doFilter(request, messageResponse);
		} catch (Throwable t) {
			logger.error("", t);
			messageResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public class MessageResponse extends HttpServletResponseWrapper {

		private final HttpServletRequest request;

		public MessageResponse(HttpServletRequest request, HttpServletResponse response) {
			super(response);
			this.request = request;
		}

		@Override
		public void sendError(int sc) throws IOException {
			sendError(sc, null);
		}

		@Override
		public void sendError(int responseCode, String message) throws IOException {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("HTTP %s %s", responseCode, message), new Exception(
						"stack trace indicates where response.sendError() is called"));
			}
			boolean acceptModelResponse = computeAcceptsModelResponse();
			if (acceptModelResponse) {
				Error error;
				switch (responseCode) {
				case SC_UNAUTHORIZED:
					error = new Error(new BadCredentialsException(message == null ? "Bad Credentials" : message));
					break;
				case SC_FORBIDDEN:
					error = new Error(new InsufficientPermissionsException(message == null ? "Access Denied" : message));
					break;
				default:
					error = new Error(new HttpStatusCodeException(responseCode));
				}
				ModelAndView modelAndView = createModelAndView("errorResponse" + responseCode, error);

				View view;
				try {
					view = viewResolver.resolveViewName(modelAndView.getViewName(),
							localeResolver.resolveLocale(request));
					setStatus(responseCode);
					view.render(modelAndView.getModel(), request, this);
				} catch (Exception e) {
					super.sendError(responseCode, message);
				}
			} else {
				super.sendError(responseCode, message);
			}
		}

		protected boolean computeAcceptsModelResponse() {
			boolean acceptModelResponse = false;
			String acceptHeader = request.getHeader("Accept");
			if (acceptHeader != null && acceptHeader.length() > 0) {
				try {
					List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
					for (MediaType acceptMediaType : mediaTypes) {
						for (MediaType modelMediaType : modelMediaTypes) {
							if (modelMediaType.includes(acceptMediaType)) {
								acceptModelResponse = true;
								break;
							}
						}
						if (acceptModelResponse) {
							break;
						}
					}
				} catch (IllegalArgumentException e) {
					// bad format
				}
			}
			return acceptModelResponse;
		}
	}

	public List<MediaType> getModelMediaTypes() {
		return modelMediaTypes;
	}

	public void setModelMediaTypes(List<MediaType> modelMediaTypes) {
		this.modelMediaTypes = modelMediaTypes;
	}
}
