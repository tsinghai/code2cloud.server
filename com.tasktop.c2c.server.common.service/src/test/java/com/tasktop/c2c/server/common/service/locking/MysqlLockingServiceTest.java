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
package com.tasktop.c2c.server.common.service.locking;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.service.locking.LockUnavailableException;
import com.tasktop.c2c.server.common.service.locking.MySqlLockingService;
import com.tasktop.c2c.server.common.service.locking.DistributedLockingService.Lock;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDBLocking.xml" })
public class MysqlLockingServiceTest {

	@Autowired
	private MySqlLockingService lockingService;

	@Test
	public void testAquireRelease() throws LockUnavailableException {
		String lockName = "foo";

		Lock lock = lockingService.obtainLock(lockName, 1000);
		Assert.assertFalse(lockingService.isLocKFree(lockName));

		try {
			lockingService.obtainLock(lockName, 1000);
			Assert.fail("expected LUE");
		} catch (LockUnavailableException e) {
			// expected
		}

		lockingService.releaseLock(lock);
		Assert.assertTrue(lockingService.isLocKFree(lockName));

		lock = lockingService.obtainLock(lockName, 0);
		Assert.assertFalse(lockingService.isLocKFree(lockName));

		try {
			lockingService.obtainLock(lockName, 1000);
			Assert.fail("expected LUE");
		} catch (LockUnavailableException e) {
			// expected
		}
		lockingService.releaseLock(lock);
		Assert.assertTrue(lockingService.isLocKFree(lockName));
	}
}
