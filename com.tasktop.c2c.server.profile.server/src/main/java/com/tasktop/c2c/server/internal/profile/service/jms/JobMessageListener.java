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
package com.tasktop.c2c.server.internal.profile.service.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import com.tasktop.c2c.server.common.service.job.Job;

@Transactional
public class JobMessageListener implements MessageListener {

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;

		Job job;
		try {
			SecurityContextHolder.createEmptyContext();
			job = (Job) objectMessage.getObject();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		} finally {
			SecurityContextHolder.clearContext();
		}

		// support for request scope
		RequestAttributesSupport attributes = new RequestAttributesSupport();
		RequestContextHolder.setRequestAttributes(attributes);
		try {
			// do the job
			job.execute(applicationContext);
		} catch (Throwable t) {
			LoggerFactory.getLogger(JobMessageListener.class.getName()).error("Cannot complete job: " + t.getMessage(),
					t);
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new RuntimeException(t);
		} finally {
			try {
				attributes.requestCompleted();
			} finally {
				// reset request scope
				RequestContextHolder.resetRequestAttributes();
			}
		}
	}
}
