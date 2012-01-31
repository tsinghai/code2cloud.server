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
package com.tasktop.c2c.server.common.service.tests;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;

import com.tasktop.c2c.server.common.service.locking.DistributedLockingService;
import com.tasktop.c2c.server.common.service.locking.LockUnavailableException;


@Ignore
public class LocalDistributedLockingService implements DistributedLockingService {

	private Set<String> outstandingLocks = new HashSet<String>();

	private static final class DLock implements Lock {
		private final String name;

		public DLock(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	@Override
	public Lock obtainLock(String lockName, long timeoutInMilliseconds) throws LockUnavailableException {
		if (outstandingLocks.contains(lockName)) {
			try {
				Thread.sleep(timeoutInMilliseconds);
			} catch (InterruptedException e) {
				throw new LockUnavailableException(e);
			}
		}
		if (outstandingLocks.contains(lockName)) {
			throw new LockUnavailableException();
		} else {
			outstandingLocks.add(lockName);
			return new DLock(lockName);
		}

	}

	@Override
	public void releaseLock(Lock lock) {
		outstandingLocks.remove(lock.getName());
	}

}
