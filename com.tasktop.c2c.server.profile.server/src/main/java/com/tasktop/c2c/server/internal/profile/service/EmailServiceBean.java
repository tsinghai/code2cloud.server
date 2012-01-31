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
package com.tasktop.c2c.server.internal.profile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.service.EmailService;

@Service("emailService")
@Transactional
public class EmailServiceBean implements EmailService {

	public static final String EMAIL_JOB_QUEUE_NAME = "EmailQueue";

	@Autowired
	private MailSender mailSender;
	@Autowired
	private SimpleMailMessage templateMessage;
	@Autowired
	private JobService jobService;

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void deliver(Email email) throws MailException {
		SimpleMailMessage message = new SimpleMailMessage(this.templateMessage);
		message.setTo(email.getTo());
		message.setSubject(email.getSubject());
		message.setText(email.getBody());
		mailSender.send(message);
	}

	@Override
	public void schedule(Email email) {
		jobService.schedule(new EmailJob(email));
	}
}
