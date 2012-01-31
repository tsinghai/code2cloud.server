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
package com.tasktop.c2c.server.cloud.service;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.common.service.NoNodeAvailableException;

public class PoolingNodeProvisioningService extends BasePoolService implements NodeProvisioningService {

	@Override
	protected int computeNumberOfNodesToDelete() {
		return 0; // Just to be sure, should never delete nodes
	}

	@Override
	public ServiceHost provisionNode() {
		try {
			return super.retrieveNodeWithMinimalAllocation();
		} catch (NoNodeAvailableException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Set a fixed pool size strategy.
	 * 
	 * @param poolSize
	 */
	public void setPoolSize(int poolSize) {
		this.poolSizeStrategy = new FixedUnderCapacityPoolSizeStrategy(poolSize);
	}
}
