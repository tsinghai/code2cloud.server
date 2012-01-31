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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.social.InsufficientPermissionException;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.cloud.service.BasePoolService;
import com.tasktop.c2c.server.cloud.service.FinishReleaseHudsonSlaveJob;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolSecurityPolicy;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.NoNodeAvailableException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.service.ConfigurationPropertyService;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;

@Transactional
public class HudsonSlavePoolServiceImpl extends BasePoolService implements HudsonSlavePoolService, InitializingBean {

	private static final String CONCURRENT_BUILD_QUOTA = "hudson.maxConcurrentBuilds";
	private static final String MAX_BUILD_TIME_QUOTA = "hudson.maxBuildTimeInMinutes";

	private int buildTimeQuotaCheckPeriod = 60 * 1000;
	private long promiseDuration = 60 * 1000;

	private class CheckBuildTimeQutaThread extends Thread {

		@Override
		public void run() {
			// TODO setup auth context. Currently this is not needed as there is not auth on the (internal) methods it
			// calls.

			while (!stopRequested.get()) {

				try {
					enforceBuildTimeQuotas();
					Thread.sleep(buildTimeQuotaCheckPeriod);
				} catch (Throwable t) {
					LOGGER.warn("Error in check build time quota thread", t);
					// Continue;
				}
			}
		}
	}

	@Autowired
	private HudsonSlavePoolSecurityPolicy securityPolicy;

	@Autowired
	private ConfigurationPropertyService configurationPropertyService;

	@Autowired
	private ProjectServiceService projectServiceService;

	@Autowired
	@Qualifier("main")
	private PromiseService promiseService;

	private Integer maxBuildTimeInMinutes = -1;

	public HudsonSlavePoolServiceImpl() {
		super.setNodeTypes(EnumSet.of(ServiceType.BUILD_SLAVE));
		super.setMaxCapacity(1);
	}

	@Override
	protected void startThreads() {
		super.startThreads();
		new CheckBuildTimeQutaThread().start();
	}

	@Override
	public RequestBuildSlaveResult acquireSlave(String projectIdentifier, String promiseTokenOrNull)
			throws ValidationException {
		this.securityPolicy.authorize(projectIdentifier);
		checkConcurrentBuildQuota(projectIdentifier);

		if (promiseTokenOrNull != null) {
			String promiseToken = promiseTokenOrNull;
			if (promiseService.isNextPromise(promiseToken)) {
				try {
					RequestBuildSlaveResult result = tryAllocateSlave(projectIdentifier);
					promiseService.redeem(promiseToken);
					return result;
				} catch (NoNodeAvailableException e) {
					return extendPromise(promiseToken);
				}
			} else {
				return extendPromise(promiseToken);
			}

		} else if (promiseService.hasOutstandingPromises()) {
			return newPromise(projectIdentifier);
		} else {
			try {
				return tryAllocateSlave(projectIdentifier);
			} catch (NoNodeAvailableException e) {
				return newPromise(projectIdentifier);
			}
		}

	}

	private RequestBuildSlaveResult newPromise(String projectIdentifier) {
		return RequestBuildSlaveResult.forPromise(promiseService.getNewPromiseToken(promiseDuration));
	}

	private RequestBuildSlaveResult extendPromise(String promiseToken) throws ValidationException {
		promiseService.extendPromise(promiseToken, promiseDuration);
		return RequestBuildSlaveResult.forPromise(promiseToken);
	}

	private RequestBuildSlaveResult tryAllocateSlave(String projectIdentifier) throws NoNodeAvailableException {
		ServiceHost nodeToLoan = super.retrieveNodeWithMinimalAllocation();
		try {
			this.serviceHostService.allocateHostToProject(ServiceType.BUILD_SLAVE, nodeToLoan, projectIdentifier);
		} catch (EntityNotFoundException e) {
			throw new IllegalStateException();
		}

		return RequestBuildSlaveResult.forSlave(nodeToLoan.getInternalNetworkAddress(), getSlaveDueDate());
	}

	private Date getSlaveDueDate() {
		if (maxBuildTimeInMinutes > 0) {
			return new Date(System.currentTimeMillis() + 1000 * 60 * maxBuildTimeInMinutes);
		}
		return null;
	}

	private void checkConcurrentBuildQuota(String projectIdentifier) {
		String maxSlavesString = configurationPropertyService.getConfigurationValue(CONCURRENT_BUILD_QUOTA);
		if (maxSlavesString == null) {
			return;
		}
		Integer maxSlaves = Integer.parseInt(maxSlavesString);
		List<ServiceHost> currentSlaves = serviceHostService.findHostsByTypeAndProject(nodeType, projectIdentifier);
		if (currentSlaves.size() >= maxSlaves) {
			throw new IllegalStateException("Already at concurrent build quota");
		}
	}

	private void enforceBuildTimeQuotas() {
		String maxBuildTimeString = configurationPropertyService.getConfigurationValue(MAX_BUILD_TIME_QUOTA);
		if (maxBuildTimeString == null) {
			return;
		}
		maxBuildTimeInMinutes = Integer.parseInt(maxBuildTimeString);
		Date killBeforeDate = new Date(System.currentTimeMillis() - (maxBuildTimeInMinutes * 1000 * 60));
		List<ProjectService> expiredServices = projectServiceService.findProjectServicesOlderThan(
				ServiceType.BUILD_SLAVE, killBeforeDate);

		for (ProjectService service : expiredServices) {
			String projectId = service.getProjectServiceProfile().getProject().getIdentifier();
			LOGGER.info(String.format("Project [%s] has exceeded the max build time of [%d]. Killing the build.",
					projectId, maxBuildTimeInMinutes));
			jobService.schedule(new FinishReleaseHudsonSlaveJob(projectId, service.getServiceHost().getId()));
		}
	}

	@Override
	public void releaseSlave(final String projectIdentifier, final String ip) {
		this.securityPolicy.authorize(projectIdentifier, ip);
		ServiceHost node = serviceHostService.findHostForIpAndType(ip, nodeType);
		if (node == null) {
			throw new InsufficientPermissionException();
		}
		jobService.schedule(new FinishReleaseHudsonSlaveJob(projectIdentifier, node.getId()));
	}

	public void setSecurityPolicy(HudsonSlavePoolSecurityPolicy securityPolicy) {
		this.securityPolicy = securityPolicy;
	}

	public void setConfigurationPropertyService(ConfigurationPropertyService configurationPropertyService) {
		this.configurationPropertyService = configurationPropertyService;
	}

	@Override
	public PoolStatus getStatus() {
		PoolStatus result = super.getStatus();
		result.setOutstandingPromises(promiseService.getNumberOfOutstandingPromises());
		return result;
	}

	/**
	 * @param promiseService
	 *            the promiseService to set
	 */
	public void setPromiseService(PromiseService promiseService) {
		this.promiseService = promiseService;
	}

	@Override
	public RequestBuildSlaveResult renewSlave(String projectIdentifier, String ip) {
		if (promiseService.hasOutstandingPromises()) {
			return RequestBuildSlaveResult.forReject();
		}

		ServiceHost node = serviceHostService.findHostForIpAndType(ip, nodeType);
		if (node == null) {
			throw new InsufficientPermissionException();
		}

		try {
			serviceHostService.deallocateHostFromProject(node, projectIdentifier);
			this.serviceHostService.allocateHostToProject(ServiceType.BUILD_SLAVE, node, projectIdentifier);
		} catch (EntityNotFoundException e) {
			throw new IllegalStateException();
		}

		return RequestBuildSlaveResult.forSlave(ip, getSlaveDueDate());
	}

}
