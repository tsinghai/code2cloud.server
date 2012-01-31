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

import java.util.HashMap;
import java.util.Map;

import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.web.AbstractTrustedHostRestServiceClient;

public class HudsonSlavePoolServiceClient extends AbstractTrustedHostRestServiceClient implements
		HudsonSlavePoolService {

	public static class ServiceCallResult {
		private RequestBuildSlaveResult requestBuildSlaveResult;
		private PoolStatus poolStatus;

		public PoolStatus getPoolStatus() {
			return poolStatus;
		}

		public void setPoolStatus(PoolStatus poolStatus) {
			this.poolStatus = poolStatus;
		}

		public RequestBuildSlaveResult getRequestBuildSlaveResult() {
			return requestBuildSlaveResult;
		}

		public void setRequestBuildSlaveResult(RequestBuildSlaveResult requestBuildSlaveResult) {
			this.requestBuildSlaveResult = requestBuildSlaveResult;
		}
	}

	public RequestBuildSlaveResult acquireSlave(String projectIdentifier, String promiseTokenOrNull)
			throws ValidationException {
		currentProjectIdentifier.set(projectIdentifier);
		try {
			String url;
			if (promiseTokenOrNull != null) {
				url = computeUrl("slave/" + projectIdentifier + "/" + promiseTokenOrNull);
			} else {
				url = computeUrl("slave/" + projectIdentifier);
			}
			return template.getForObject(url, ServiceCallResult.class).getRequestBuildSlaveResult();
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		} finally {
			currentProjectIdentifier.remove();
		}
	}

	public RequestBuildSlaveResult renewSlave(String projectIdentifier, String ip) {
		currentProjectIdentifier.set(projectIdentifier);
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("ip", ip);
			args.put("projectIdentifier", projectIdentifier);

			return template.getForObject(computeUrl("slave/{projectIdentifier}/{ip}/renew"), ServiceCallResult.class,
					args).getRequestBuildSlaveResult();
		} finally {
			currentProjectIdentifier.remove();
		}
	}

	public void releaseSlave(String projectIdentifier, String ip) {
		currentProjectIdentifier.set(projectIdentifier);

		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("ip", ip);
			args.put("projectIdentifier", projectIdentifier);

			template.getForObject(computeUrl("slave/{projectIdentifier}/{ip}/release"), Void.class, args);
		} finally {
			currentProjectIdentifier.remove();
		}
	}

	public PoolStatus getStatus() {
		return template.getForObject(computeUrl("status/"), PoolStatus.class);
	}

}
