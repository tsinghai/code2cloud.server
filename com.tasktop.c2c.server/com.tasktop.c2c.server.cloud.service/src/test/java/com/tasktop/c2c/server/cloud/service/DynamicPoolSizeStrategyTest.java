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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.cloud.service.DynamicPoolSizeStrategy;
import com.tasktop.c2c.server.cloud.service.PoolSizeStrategy.PoolLoad;
import com.tasktop.c2c.server.cloud.service.PoolSizeStrategy.PoolSize;


public class DynamicPoolSizeStrategyTest {

	private DynamicPoolSizeStrategy dynamicPoolSizeStrategy;
	private int maxTotalNodes = 100;
	private int maxUnderCapacityNodes = 2;
	private int minUnderCapacityNodes = 1;
	private int minTotalNodes = 2;
	private double excessLoadAllocationFactor = .8;

	@Before
	public void setup() {
		dynamicPoolSizeStrategy = new DynamicPoolSizeStrategy();
		dynamicPoolSizeStrategy.setMaxTotalNodes(maxTotalNodes);
		dynamicPoolSizeStrategy.setMinTotalNodes(minTotalNodes);
		dynamicPoolSizeStrategy.setMaxUnderCapacityNodes(maxUnderCapacityNodes);
		dynamicPoolSizeStrategy.setMinUnderCapacityNodes(minUnderCapacityNodes);
	}

	@Test
	public void testNoChange() {
		int numAllocated = 5;
		int numFree = maxUnderCapacityNodes;
		int currentSize = numAllocated + numFree;
		PoolSize size = dynamicPoolSizeStrategy.getDesiredPoolSize(getPoolLoad(numAllocated, numFree));
		Assert.assertEquals(currentSize, size.getLowerBound());
		Assert.assertEquals(currentSize, size.getUpperBound());
	}

	@Test
	public void testCull() {
		PoolSize size = dynamicPoolSizeStrategy.getDesiredPoolSize(getPoolLoad(0, maxUnderCapacityNodes + 5));
		Assert.assertEquals(maxUnderCapacityNodes, size.getLowerBound());
		Assert.assertEquals(maxUnderCapacityNodes, size.getUpperBound());

		// // NO cull, we are at our limit
		size = dynamicPoolSizeStrategy.getDesiredPoolSize(getPoolLoad(1, maxUnderCapacityNodes));
		Assert.assertEquals(1 + maxUnderCapacityNodes, size.getLowerBound());
		Assert.assertEquals(1 + maxUnderCapacityNodes, size.getUpperBound());

	}

	@Test
	public void testGrowWhenFullyAllocated() {
		PoolSize size = dynamicPoolSizeStrategy.getDesiredPoolSize(getPoolLoad(10, 0));
		Assert.assertEquals(10 + minUnderCapacityNodes, size.getLowerBound());
		Assert.assertEquals(10 + minUnderCapacityNodes, size.getUpperBound());
	}

	private PoolLoad getPoolLoad(int numAllocated, int numFree) {
		PoolLoad result = new PoolLoad();
		result.setTotalNodesAtCapacity(numAllocated);
		result.setTotalNodesBelowCapacity(numFree);
		result.setTotalNodes(numFree + numAllocated);
		return result;
	}
}
