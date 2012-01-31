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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;


import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.service.EmailService;

public class EmailJob extends Job {

	private static final long serialVersionUID = -5707948559930108642L;

	private Email email;

	public EmailJob(Email email) {
		super(Type.EMAIL);
		this.email = email;
	}

	@Override
	public void execute(ApplicationContext applicationContext) throws BeansException {
		EmailService emailService = applicationContext.getBean("emailService", EmailService.class);
		emailService.deliver(email);
	}

	public Email getEmail() {
		return email;
	}
}
