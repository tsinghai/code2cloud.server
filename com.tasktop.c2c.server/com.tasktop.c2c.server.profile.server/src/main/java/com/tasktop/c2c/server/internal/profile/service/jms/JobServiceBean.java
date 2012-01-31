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
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.common.service.job.JobService;


@Service("jobService")
@Transactional
public class JobServiceBean implements JobService {

	public static final String JOB_TYPE_PROPERTY = "Type";

	@Autowired
	private JmsTemplate template;

	@Override
	public void schedule(final Job job) {
		template.send(new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				Message message = session.createObjectMessage(job);
				message.setStringProperty(JOB_TYPE_PROPERTY, job.getType().name());
				return message;
			}
		});

	}

	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

}
