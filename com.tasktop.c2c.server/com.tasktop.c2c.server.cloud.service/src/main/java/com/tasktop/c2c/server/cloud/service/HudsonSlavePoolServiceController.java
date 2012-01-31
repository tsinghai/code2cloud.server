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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;

public class HudsonSlavePoolServiceController extends AbstractRestService implements HudsonSlavePoolService {

	private static HudsonSlavePoolServiceController lastInstance = null;

	/** For testing. */
	public static void setLastInstantiatedService(HudsonSlavePoolService service) {
		lastInstance.service = service;
	}

	public void setService(HudsonSlavePoolService service) {
		this.service = service;
	}

	public HudsonSlavePoolServiceController() {
		lastInstance = this;
	}

	private HudsonSlavePoolService service;

	@RequestMapping(value = "slave/{projectIdentifier}", method = RequestMethod.GET)
	public RequestBuildSlaveResult doAcquireSlave(@PathVariable("projectIdentifier") String projectIdentifier)
			throws ValidationException {
		return acquireSlave(projectIdentifier, null);
	}

	@RequestMapping(value = "slave/{projectIdentifier}/{promiseToken}", method = RequestMethod.GET)
	public RequestBuildSlaveResult doAcquireSlave(@PathVariable("projectIdentifier") String projectIdentifier,
			@PathVariable("promiseToken") String promiseToken) throws ValidationException {
		return acquireSlave(projectIdentifier, promiseToken);
	}

	@Override
	public RequestBuildSlaveResult acquireSlave(String projectIdentifier, String promiseTokenOrNull)
			throws ValidationException {
		return service.acquireSlave(projectIdentifier, promiseTokenOrNull);
	}

	@Override
	@RequestMapping(value = "slave/{projectIdentifier}/{ip}/renew", method = RequestMethod.GET)
	public RequestBuildSlaveResult renewSlave(@PathVariable("projectIdentifier") String projectIdentifier,
			@PathVariable("ip") String ip) {
		return service.renewSlave(projectIdentifier, ip);
	}

	@Override
	@RequestMapping(value = "slave/{projectIdentifier}/{ip}/release", method = RequestMethod.GET)
	public void releaseSlave(@PathVariable("projectIdentifier") String projectIdentifier, @PathVariable("ip") String ip) {
		service.releaseSlave(projectIdentifier, ip);
	}

	@Override
	@RequestMapping(value = "status", method = RequestMethod.GET)
	public PoolStatus getStatus() {
		return service.getStatus();
	}

}
