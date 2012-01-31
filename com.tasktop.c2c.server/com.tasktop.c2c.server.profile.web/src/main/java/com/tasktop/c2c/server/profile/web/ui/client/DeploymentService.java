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

import java.io.Serializable;
import java.util.List;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentStatus;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@RemoteServiceRelativePath("deploymentService")
public interface DeploymentService extends RemoteService {

	/** Container class. */
	public static class DeploymentConfigurationOptions implements Serializable {
		private List<Integer> availableMemories;
		private List<CloudService> availableServices;
		private List<DeploymentServiceConfiguration> availableServiceConfigurations;
		private AvailableBuildInformation buildInformation;

		/**
		 * @return the availableMemories
		 */
		public List<Integer> getAvailableMemories() {
			return availableMemories;
		}

		/**
		 * @param availableMemories
		 *            the availableMemories to set
		 */
		public void setAvailableMemories(List<Integer> availableMemories) {
			this.availableMemories = availableMemories;
		}

		/**
		 * @return the availableServices
		 */
		public List<CloudService> getAvailableServices() {
			return availableServices;
		}

		/**
		 * @param availableServices
		 *            the availableServices to set
		 */
		public void setAvailableServices(List<CloudService> availableServices) {
			this.availableServices = availableServices;
		}

		/**
		 * @return the availableServiceConfigurations
		 */
		public List<DeploymentServiceConfiguration> getAvailableServiceConfigurations() {
			return availableServiceConfigurations;
		}

		/**
		 * @param availableServiceConfigurations
		 *            the availableServiceConfigurations to set
		 */
		public void setAvailableServiceConfigurations(
				List<DeploymentServiceConfiguration> availableServiceConfigurations) {
			this.availableServiceConfigurations = availableServiceConfigurations;
		}

		/**
		 * @return the buildInformation
		 */
		public AvailableBuildInformation getBuildInformation() {
			return buildInformation;
		}

		/**
		 * @param buildInformation
		 *            the buildInformation to set
		 */
		public void setBuildInformation(AvailableBuildInformation buildInformation) {
			this.buildInformation = buildInformation;
		}
	}

	public static class AvailableBuildInformation implements Serializable {
		private List<String> buildJobNames;
		private List<BuildDetails> builds;

		/**
		 * @return the buildJobNames
		 */
		public List<String> getBuildJobNames() {
			return buildJobNames;
		}

		/**
		 * @param buildJobNames
		 *            the buildJobNames to set
		 */
		public void setBuildJobNames(List<String> buildJobNames) {
			this.buildJobNames = buildJobNames;
		}

		/**
		 * @return the builds
		 */
		public List<BuildDetails> getBuilds() {
			return builds;
		}

		/**
		 * @param builds
		 *            the builds to set
		 */
		public void setBuilds(List<BuildDetails> builds) {
			this.builds = builds;
		}

	}

	public List<DeploymentConfiguration> getDeploymentConfigurations(String projectId);

	public DeploymentConfiguration createDeploymentConfiguration(String projectId, DeploymentConfiguration configuration)
			throws ValidationFailedException;

	public DeploymentConfiguration updateDeployment(DeploymentConfiguration configuration)
			throws NoSuchEntityException, ValidationFailedException;

	public void deleteDeployment(DeploymentConfiguration configuration, boolean alsoDeleteFromCF)
			throws NoSuchEntityException;

	public DeploymentStatus startDeployment(DeploymentConfiguration configuration) throws ValidationFailedException;

	public DeploymentStatus stopDeployment(DeploymentConfiguration configuration) throws ValidationFailedException;

	public DeploymentStatus restartDeployment(DeploymentConfiguration configuration) throws ValidationFailedException;

	public Boolean validateCredentials(DeploymentConfiguration configuration);

	public DeploymentConfigurationOptions getDeploymentConfigurationOptions(String projectId,
			DeploymentConfiguration configuration) throws NoSuchEntityException;

	/**
	 * Get the buildInfo.
	 * 
	 * @param projectIdentifier
	 * @param buildJobName
	 *            : or null for all names
	 * @return
	 */
	public AvailableBuildInformation getBuildInformation(String projectIdentifier, String buildJobName);

	public CloudService createService(DeploymentConfiguration configuration, CloudService service);

}
