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

import java.util.List;

public interface PoolSizeStrategy {

	public class PoolLoad {
		private int totalNodes;
		private int totalNodesAtCapacity;
		private int totalNodesBelowCapacity;
		private List<Double> individualNodeCapacities;
		private int excessCapacity;

		public int getTotalNodes() {
			return totalNodes;
		}

		public void setTotalNodes(int totalNodes) {
			this.totalNodes = totalNodes;
		}

		public int getTotalNodesAtCapacity() {
			return totalNodesAtCapacity;
		}

		public void setTotalNodesAtCapacity(int totalNodesAtCapacity) {
			this.totalNodesAtCapacity = totalNodesAtCapacity;
		}

		public int getTotalNodesBelowCapacity() {
			return totalNodesBelowCapacity;
		}

		public void setTotalNodesBelowCapacity(int totalNodesBelowCapacity) {
			this.totalNodesBelowCapacity = totalNodesBelowCapacity;
		}

		public List<Double> getIndividualNodeCapacities() {
			return individualNodeCapacities;
		}

		public void setIndividualNodeCapacities(List<Double> individualNodeCapacities) {
			this.individualNodeCapacities = individualNodeCapacities;
		}

		public int getExcessCapacity() {
			return excessCapacity;
		}

		public void setExcessCapacity(int excessCapacity) {
			this.excessCapacity = excessCapacity;
		}
	}

	public class PoolSize {
		private int lowerBound;
		private int upperBound;

		public PoolSize(int lowerBound, int upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		public int getLowerBound() {
			return lowerBound;
		}

		public void setLowerBound(int lowerBound) {
			this.lowerBound = lowerBound;
		}

		public int getUpperBound() {
			return upperBound;
		}

		public void setUpperBound(int upperBound) {
			this.upperBound = upperBound;
		}
	}

	/**
	 * 
	 * @param poolLoad
	 * @return
	 */
	PoolSize getDesiredPoolSize(PoolLoad poolLoad);
}
