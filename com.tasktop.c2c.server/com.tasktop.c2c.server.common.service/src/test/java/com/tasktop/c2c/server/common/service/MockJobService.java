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

import java.util.ArrayList;
import java.util.List;

import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.common.service.job.JobService;


public class MockJobService implements JobService {

	private List<Job> scheduledJobs = new ArrayList<Job>();

	@Override
	public void schedule(Job job) {
		scheduledJobs.add(job);
	}

	public List<Job> getScheduledJobs() {
		return scheduledJobs;
	}

}
