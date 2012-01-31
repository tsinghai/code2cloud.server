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
package com.tasktop.c2c.server.profile.domain.validation;


import com.tasktop.c2c.server.common.service.validation.AbstractDomainValidator;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.internal.deployment.service.DeploymentConfigurationValidator;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;
import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;

public class DomainValidator extends AbstractDomainValidator {

	{
		registerValidator(Profile.class, new ProfileValidator());
		registerValidator(Project.class, new ProjectValidator());
		registerValidator(ProjectProfile.class, new ProjectProfileValidator());
		registerValidator(ScmRepository.class, new ScmRepositoryValidator());
		registerValidator(SignUpToken.class, new SignUpTokenValidator());
		registerValidator(DeploymentConfiguration.class, new DeploymentConfigurationValidator());
		registerValidator(SshPublicKey.class, new SshPublicKeyValidator());
		registerValidator(SshPublicKeySpec.class, new SshPublicKeySpecValidator());
	}

}
