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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class MySqlLockingService implements DistributedLockingService {

	private DataSource dataSource;

	private static final class MySqlLock implements Lock {
		private final String name;
		private final Connection connection;

		public MySqlLock(String name, Connection connection) {
			this.name = name;
			this.connection = connection;
		}

		public String getName() {
			return name;
		}

		public Connection getConnection() {
			return connection;
		}
	}

	@Override
	public Lock obtainLock(String lockName, long timeoutInMilliseconds) throws LockUnavailableException {
		int timeoutInSeconds = (int) (timeoutInMilliseconds / 1000);

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			ResultSet rs = connection.createStatement().executeQuery(
					"SELECT GET_LOCK('" + lockName + "', " + timeoutInSeconds + ")");

			Long gotIt = null;
			if (rs.first()) {
				gotIt = (Long) rs.getObject(1);
			}

			if (gotIt == null || gotIt.equals(0l)) {
				connection.close();
				throw new LockUnavailableException();
			} else {
				return new MySqlLock(lockName, connection);
			}
		} catch (SQLException e) {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException(e);
		}
	}

	public boolean isLocKFree(String lockName) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();

			ResultSet rs = connection.createStatement().executeQuery("SELECT IS_FREE_LOCK('" + lockName + "')");

			Long isFree = null;
			if (rs.first()) {
				isFree = (Long) rs.getObject(1);
			}
			return (isFree != null && isFree.equals(1l));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void releaseLock(Lock lock) {
		if (!(lock instanceof MySqlLock)) {
			throw new IllegalArgumentException();
		}
		MySqlLock sqlLock = (MySqlLock) lock;
		try {
			sqlLock.getConnection().createStatement().execute("SELECT RELEASE_LOCK('" + lock.getName() + "')");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				sqlLock.getConnection().close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
