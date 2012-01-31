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
package com.tasktop.c2c.server.common.service.job;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

/**
 * A simple job service implemented using {@link ExecutorService}. Not recommended for jobs that require retry or
 * durability.
 * 
 * Example Spring configuration:
 * 
 * <pre>
 * <code>
 * &lt;bean id="jobService" class="com.tasktop.c2c.server.common.service.job.ExecutorJobService" autowire="byType">
 * 		&lt;property name="executor">
 * 			&lt;bean factory-method="newFixedThreadPool"  class="java.util.concurrent.Executors">
 * 				&lt;constructor-arg type="int" value="1"/>
 * 			&lt;/bean>
 * 		&lt;/property>
 * &lt;/bean>
 * </code>
 * </pre>
 */
public class ExecutorJobService implements JobService, DisposableBean {

	private Logger log = LoggerFactory.getLogger(ExecutorJobService.class.getName());

	private ExecutorService executor;

	private ApplicationContext applicationContext;

	@Override
	public void schedule(Job job) {
		executor.submit(new JobCallable(job));
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void destroy() throws Exception {
		List<Runnable> runnables = executor.shutdownNow();
		if (!runnables.isEmpty()) {
			log.warn(String.format("Shutting down executor service with %s outstanding jobs", runnables.size()));
		}
	}

	public class JobCallable implements Callable<Object> {

		private final Job job;

		public JobCallable(Job job) {
			this.job = job;
		}

		@Override
		public Object call() throws Exception {
			job.execute(applicationContext);
			return null;
		}

	}
}
