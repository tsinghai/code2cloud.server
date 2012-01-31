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
package com.tasktop.c2c.server.profile.web.ui.client;

import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentService.AvailableBuildInformation;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentService.DeploymentConfigurationOptions;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface DeploymentServiceAsync {

	/**
	 * 
	 * @see com.tasktop.c2c.server.profile.web.ui.client.DeploymentService#getDeploymentConfigurations(java.lang.String)
	 */
	void getDeploymentConfigurations(String projectId, AsyncCallback<List<DeploymentConfiguration>> callback);

	void createDeploymentConfiguration(String projectId, DeploymentConfiguration configuration,
			AsyncCallback<DeploymentConfiguration> callback);

	void validateCredentials(DeploymentConfiguration configuration, AsyncCallback<Boolean> callback);

	void updateDeployment(DeploymentConfiguration configuration, AsyncCallback<DeploymentConfiguration> callback);

	void getDeploymentConfigurationOptions(String projectId, DeploymentConfiguration configuration,
			AsyncCallback<DeploymentConfigurationOptions> callback);

	void deleteDeployment(DeploymentConfiguration configuration, boolean alsoDeleteFromCF, AsyncCallback<Void> callback);

	void startDeployment(DeploymentConfiguration configuration, AsyncCallback<DeploymentStatus> callback);

	void stopDeployment(DeploymentConfiguration configuration, AsyncCallback<DeploymentStatus> callback);

	void restartDeployment(DeploymentConfiguration configuration, AsyncCallback<DeploymentStatus> callback);

	void createService(DeploymentConfiguration configuration, CloudService service, AsyncCallback<CloudService> callback);

	void getBuildInformation(String projectIdentifier, String buildJobName,
			AsyncCallback<AvailableBuildInformation> callback);

}
