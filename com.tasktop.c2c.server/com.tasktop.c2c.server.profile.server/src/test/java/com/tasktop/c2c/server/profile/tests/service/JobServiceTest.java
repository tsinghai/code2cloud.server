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
package com.tasktop.c2c.server.profile.tests.service;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.common.service.job.JobService;

@Ignore("Turned off to avoid build hangs on the CI server caused by a JMS broker issue")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testNoRollback.xml" })
public class JobServiceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JobService jobService;

	@Autowired
	private PlatformTransactionManager trxManager;

	public static class TestJob extends Job {

		@Override
		public void execute(ApplicationContext applicationContext) {
			numTestJobExecutions++;
		}

	}

	public static class TestParallelJob extends Job {

		@Override
		public void execute(ApplicationContext applicationContext) {
			numParallelJobs.incrementAndGet();

			while (numParallelJobs.get() < expectedParallelJobs) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			numTestJobExecutions++;
		}

	}

	public static class TestJobThatFailsOnce extends Job {

		@Override
		public void execute(ApplicationContext applicationContext) {
			numTestJobExecutions++;

			if (numFailures < 1) {
				numFailures++;
				throw new RuntimeException();
			}
		}

	}

	public static class TestJobThatAlwaysFails extends Job {

		@Override
		public void execute(ApplicationContext applicationContext) {
			numTestJobExecutions++;
			numFailures++;
			throw new RuntimeException();

		}

	}

	private static AtomicInteger numParallelJobs = new AtomicInteger(0);
	private static int expectedParallelJobs = 0;
	private static int numTestJobExecutions = 0;
	private static int numFailures = 0;

	@Before
	public void setup() {
		numTestJobExecutions = 0;
		numFailures = 0;
	}

	private void scheduleJobInTransaction(final Job job) {
		scheduleJobInTransaction(job, false);
	}

	private void scheduleJobInTransaction(final Job job, final boolean throwExceptionAfterScheduling) {
		new TransactionTemplate(trxManager).execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				jobService.schedule(job);
				if (throwExceptionAfterScheduling) {
					throw new RuntimeException();
				}
				return null;
			}
		});
	}

	@Test
	public void testSimpleJob() throws InterruptedException {
		scheduleJobInTransaction(new TestJob());

		Thread.sleep(100);
		Assert.assertEquals(1, numTestJobExecutions);

	}

	@Test
	public void testParallelJobs() throws InterruptedException {
		expectedParallelJobs = 2;
		scheduleJobInTransaction(new TestParallelJob());
		scheduleJobInTransaction(new TestParallelJob());
		Thread.sleep(10000);
		Assert.assertEquals(expectedParallelJobs, numTestJobExecutions);
	}

	@Test
	public void testScheduleJobIsRolledBack() throws InterruptedException {
		try {
			scheduleJobInTransaction(new TestJob(), true);
			Assert.fail("expect exception");
		} catch (RuntimeException e) {
			// Expected
		}
		Thread.sleep(100);
		Assert.assertEquals(0, numTestJobExecutions);

	}

	@Test
	public void testExceptionInJob() throws InterruptedException {
		scheduleJobInTransaction(new TestJobThatFailsOnce());

		Thread.sleep(1000);
		Assert.assertEquals(1, numFailures);
		Assert.assertEquals(2, numTestJobExecutions);

	}

	@Test
	@Ignore
	public void testJobAlwaysFails() throws InterruptedException {
		scheduleJobInTransaction(new TestJobThatAlwaysFails());

		Thread.sleep(400);
		Assert.assertTrue(numTestJobExecutions > 0);
		Assert.assertTrue(numFailures > 0);
		// XXX where does this setup live?
	}
}
