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

import com.tasktop.c2c.server.common.service.ValidationException;

/**
 * Interface for interacting with the the hudson slave pool. This is intended to be used by the hudson master to
 * checkout and release slave machines (VMs) that can be used to run builds. This service is run on a hub node and
 * exposed to the masters as a REST serivice.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface HudsonSlavePoolService {

	static final class PoolStatus {
		private int totalNodes;
		private int freeNodes;
		private int nodesOnLoan;
		private int outstandingPromises;

		public int getTotalNodes() {
			return totalNodes;
		}

		public void setTotalNodes(int totalNodes) {
			this.totalNodes = totalNodes;
		}

		public int getFreeNodes() {
			return freeNodes;
		}

		public void setFreeNodes(int freeNodes) {
			this.freeNodes = freeNodes;
		}

		public int getNodesOnLoan() {
			return nodesOnLoan;
		}

		public void setNodesOnLoan(int nodesOnLoan) {
			this.nodesOnLoan = nodesOnLoan;
		}

		public int getOutstandingPromises() {
			return outstandingPromises;
		}

		public void setOutstandingPromises(int outstandingPromises) {
			this.outstandingPromises = outstandingPromises;
		}
	}

	RequestBuildSlaveResult acquireSlave(String projectIdentifier, String promiseTokenOrNull)
			throws ValidationException;

	RequestBuildSlaveResult renewSlave(String projectIdentifer, String ip);

	void releaseSlave(String projectIdentifier, String ip);

	PoolStatus getStatus();
}
