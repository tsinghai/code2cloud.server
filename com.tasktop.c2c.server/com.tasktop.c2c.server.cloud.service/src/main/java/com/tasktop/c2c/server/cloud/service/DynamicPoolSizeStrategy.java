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

public class DynamicPoolSizeStrategy implements PoolSizeStrategy {

	/** Get above this, we want to start culling nodes. */
	private int maxUnderCapacityNodes;

	/** Get below this, we want to start creating nodes. */
	private int minUnderCapacityNodes = 1;

	/** Hard MAX on the total nodes of the pool. */
	private int maxTotalNodes;

	/** Hard MIN on the total nodes of the pool. */
	private int minTotalNodes;

	@Override
	public PoolSize getDesiredPoolSize(PoolLoad load) {
		int desiredSize = load.getTotalNodes();

		// Check if we need to cull/grow based on FREE nodes
		if (load.getTotalNodesBelowCapacity() > maxUnderCapacityNodes) {
			int numToCull = load.getTotalNodesBelowCapacity() - maxUnderCapacityNodes;
			desiredSize = desiredSize - numToCull;
		} else if (load.getTotalNodesBelowCapacity() < minUnderCapacityNodes) {
			int numToAdd = minUnderCapacityNodes - load.getTotalNodesBelowCapacity();
			desiredSize = desiredSize + numToAdd;
		}

		desiredSize = Math.min(desiredSize, maxTotalNodes);
		desiredSize = Math.max(desiredSize, minTotalNodes);
		return new PoolSize(desiredSize, desiredSize);
	}

	public void setMaxUnderCapacityNodes(int maxUnderCapacityNodes) {
		this.maxUnderCapacityNodes = maxUnderCapacityNodes;
	}

	public void setMaxTotalNodes(int maxTotalNodes) {
		this.maxTotalNodes = maxTotalNodes;
	}

	public void setMinTotalNodes(int minTotalNodes) {
		this.minTotalNodes = minTotalNodes;
	}

	public void setMinUnderCapacityNodes(int minUnderCapacityNodes) {
		this.minUnderCapacityNodes = minUnderCapacityNodes;
	}

}
