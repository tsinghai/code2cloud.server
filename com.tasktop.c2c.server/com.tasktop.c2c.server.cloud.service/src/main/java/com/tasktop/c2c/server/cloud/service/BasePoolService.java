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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService.PoolStatus;
import com.tasktop.c2c.server.cloud.service.PoolSizeStrategy.PoolLoad;
import com.tasktop.c2c.server.cloud.service.PoolSizeStrategy.PoolSize;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.NoNodeAvailableException;
import com.tasktop.c2c.server.common.service.job.JobService;

public class BasePoolService implements InitializingBean {

	protected static final Logger LOGGER = LoggerFactory.getLogger(BasePoolService.class);

	private int updatePeriod = 30 * 1000;

	protected int maxCapacity = 100;
	protected Set<ServiceType> nodeType;
	private PoolSize desiredPoolSize;

	protected ServiceHostService serviceHostService;
	protected PoolSizeStrategy poolSizeStrategy;

	@Autowired
	protected JobService jobService;
	@Autowired
	protected PlatformTransactionManager trxManager;

	protected WorkerThread thread;
	protected AtomicBoolean stopRequested = new AtomicBoolean(false);

	private boolean startThreads = true;

	private class WorkerThread extends Thread {

		@Override
		public void run() {
			while (!stopRequested.get()) {

				try {
					doBackgroundWork();
					Thread.sleep(updatePeriod);
				} catch (Throwable t) {
					LOGGER.warn("Error in [" + nodeType + "] pool service", t);
					// Continue;
					try {
						Thread.sleep(updatePeriod);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	protected void doBackgroundWork() {
		updateDesiredPoolSize();
		maybeAllocateNodes();
		maybeDeleteNodes();

	}

	private PoolLoad calculatePoolLoad() {
		return new TransactionTemplate(trxManager).execute(new TransactionCallback<PoolLoad>() {

			@Override
			public PoolLoad doInTransaction(TransactionStatus status) {
				PoolLoad load = new PoolLoad();
				load.setTotalNodesBelowCapacity(getNumNodesBelowCapacity());
				load.setTotalNodesAtCapacity(getNumNodesAtCapacity());
				load.setTotalNodes(getNumTotalNodesInPool());

				return load;
			}
		});
	}

	protected void updateDesiredPoolSize() {
		desiredPoolSize = poolSizeStrategy.getDesiredPoolSize(calculatePoolLoad());
	}

	protected void maybeAllocateNodes() {
		new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				int numNodesToProvision = computeNumberOfNodesToProvision();
				if (numNodesToProvision != 0) {
					LOGGER.info(String.format("Provisioning [%d] nodes of type [%s]", numNodesToProvision, nodeType));
				}
				for (int i = 0; i < numNodesToProvision; i++) {
					serviceHostService.recordAllocationScheduled(nodeType);
					jobService.schedule(new ServiceHostProvisioningJob(nodeType));
				}

			}
		});
	}

	// XXX This logic only really works when capacity is 1. Otherwise we need to migrate data off the node before we
	// decommision.
	protected void maybeDeleteNodes() {
		new TransactionTemplate(trxManager).execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				int numNodesToDelete = computeNumberOfNodesToDelete();
				if (numNodesToDelete != 0) {
					LOGGER.info(String.format("Decomission-ing [%d] nodes of type [%s]", numNodesToDelete, nodeType));
				}
				for (int i = 0; i < numNodesToDelete; i++) {
					try {
						List<ServiceHost> nodes = serviceHostService.findHostsBelowCapacity(nodeType, maxCapacity);
						if (nodes.isEmpty()) {
							throw new IllegalStateException();
						}
						ServiceHost nodeToDecommision = nodes.get(0);
						LOGGER.info(String.format("Decomission-ing nodes[%s]", nodeToDecommision.toString()));
						serviceHostService.removeServiceHost(nodeToDecommision);
						jobService.schedule(new ServiceHostDeletionJob(new HashSet<ServiceType>(nodeToDecommision
								.getSupportedServices()), nodeToDecommision.getInternalNetworkAddress()));
					} catch (EntityNotFoundException e) {
						throw new RuntimeException(e);
					}

				}
			}
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.nodeType, "A node type is required.");
		Assert.notNull(this.jobService, "A job managment service is required.");
		Assert.notNull(this.poolSizeStrategy, "A pool size strategy is required.");
		Assert.notNull(this.trxManager, "A transaction manager is required.");
		Assert.notNull(this.serviceHostService, "A service host service is required.");

		initialize();
	}

	public void initialize() {
		if (startThreads && this.thread == null) {
			startThreads();
		}
	}

	protected void startThreads() {
		this.thread = new WorkerThread();
		this.thread.start();
	}

	public ServiceHost retrieveNodeWithMinimalAllocation() throws NoNodeAvailableException {
		List<ServiceHost> hosts = serviceHostService.findHostsBelowCapacity(this.nodeType, this.maxCapacity);
		if (hosts.isEmpty()) {
			throw new NoNodeAvailableException("No node available");
		}
		return hosts.get(0);
	}

	public int getNumNodesBelowCapacity() {
		int numNodes = serviceHostService.findHostsBelowCapacity(nodeType, maxCapacity).size();
		LOGGER.debug("Nodes below capacity: [" + numNodes + "]");
		return numNodes;
	}

	public int getNumNodesAtCapacity() {
		int numNodes = serviceHostService.findHostsAtCapacity(nodeType, maxCapacity).size();
		LOGGER.debug("Nodes at capacity: [" + numNodes + "]");
		return numNodes;
	}

	Integer lastResult = null;

	public int getNumTotalNodesInPool() {
		int numNodes = serviceHostService.findHostsByType(nodeType).size();
		LOGGER.debug("Total nodes: [" + numNodes + "]");
		return numNodes;
	}

	protected int computeNumberOfNodesToProvision() {
		return desiredPoolSize.getLowerBound() - (getNumTotalNodesInPool() + getNumAllocatingNodes());
	}

	private int getNumAllocatingNodes() {
		int numAllocating = serviceHostService.getNumAllocatingNodes(this.nodeType);
		LOGGER.debug("Num Allocating: [" + numAllocating + "]");
		return numAllocating;
	}

	protected int computeNumberOfNodesToDelete() {
		return getNumTotalNodesInPool() - desiredPoolSize.getUpperBound();
	}

	public boolean isAllocating() {
		updateDesiredPoolSize();
		return computeNumberOfNodesToProvision() > 0 || getNumAllocatingNodes() > 0
				|| computeNumberOfNodesToDelete() > 0;
	}

	public void shutdown() {
		this.stopRequested.set(true);
	}

	public void setUpdatePeriod(int updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public void setNodeTypes(Set<ServiceType> nodeType) {
		this.nodeType = nodeType;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public void setServiceHostService(ServiceHostService serviceHostService) {
		this.serviceHostService = serviceHostService;
	}

	public PoolStatus getStatus() {
		PoolStatus result = new PoolStatus();
		result.setTotalNodes(getNumTotalNodesInPool());
		result.setFreeNodes(getNumNodesBelowCapacity());
		result.setNodesOnLoan(getNumNodesAtCapacity());

		return result;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	public void setStartThreads(boolean startThreads) {
		this.startThreads = startThreads;
	}

	public void setTrxManager(PlatformTransactionManager trxManager) {
		this.trxManager = trxManager;
	}

	public void setPoolSizeStrategy(PoolSizeStrategy poolSizeStrategy) {
		this.poolSizeStrategy = poolSizeStrategy;
	}

}
