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
package com.tasktop.c2c.server.common.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.util.StopWatch;

import com.tasktop.c2c.server.common.service.logging.NoLog;

public class ServiceLoggingAdvice {

	private Logger logger = LoggerFactory.getLogger("Service");

	public Object doLogging(ProceedingJoinPoint pjp) throws Throwable {
		StopWatch stopWatch = new StopWatch();

		Throwable t = null;
		Object result = null;
		try {
			stopWatch.start();
			result = pjp.proceed();
		} catch (Throwable e) {
			t = e;
		} finally {
			stopWatch.stop();
		}

		StringBuilder msg = new StringBuilder();
		logCall(pjp, t, result, msg);

		msg.append(" (time: ").append(stopWatch.getLastTaskTimeMillis() + "ms)");
		msg.append(" (user: ");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			msg.append("<NO AUTH>"); // Should never hapen
		} else if (auth instanceof AnonymousAuthenticationToken) {
			msg.append("<ANONYMOUS>");
		} else {
			msg.append(auth.getName());
		}
		msg.append(")");

		msg.append(" (project: ").append(getProjectId()).append(")");

		logger.info(msg.toString());

		if (t != null) {
			throw t;
		} else {
			return result;
		}
	}

	private void logCall(ProceedingJoinPoint pjp, Throwable t, Object result, StringBuilder msg)
			throws NoSuchMethodException {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		msg.append(pjp.getSignature().getDeclaringType().getSimpleName()).append(".")
				.append(pjp.getSignature().getName()).append("(");

		if (method.getAnnotation(NoLog.class) != null) {
			msg.append("***)");
			return;
		}

		boolean needSep = false;
		int i = 0;
		for (Object arg : pjp.getArgs()) {
			if (needSep) {
				msg.append(", ");
			} else {
				needSep = true;
			}

			if (!shouldLog(i++, method)) {
				msg.append("***");
			} else {
				msg.append(String.valueOf(arg));
			}
		}
		msg.append(") -> ");
		if (t != null) {
			msg.append("threw ").append(t.toString());
		} else {
			msg.append(result);
		}
	}

	private boolean shouldLog(int argNum, Method method) {
		Annotation[] annotations = method.getParameterAnnotations()[argNum];
		for (Annotation a : annotations) {
			if (a instanceof NoLog) {
				return false;
			}
		}
		return true;
	}

	private String getProjectId() {
		if (TenancyContextHolder.getContext().getTenant() == null
				|| TenancyContextHolder.getContext().getTenant().getIdentity() == null) {
			return null;
		}
		return TenancyContextHolder.getContext().getTenant().getIdentity().toString();
	}
}
