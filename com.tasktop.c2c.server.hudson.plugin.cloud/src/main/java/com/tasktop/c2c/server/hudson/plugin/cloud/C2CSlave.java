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
package com.tasktop.c2c.server.hudson.plugin.cloud;

import hudson.model.Descriptor.FormException;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudSlave;

import java.io.IOException;
import java.util.Collections;

public class C2CSlave extends AbstractCloudSlave {
	private static final String remoteFS = "/home/c2c/hudson";
	private static final int numExecutors = 1;
	private static final Mode mode = Mode.NORMAL;
	private static final String label = "Code2Cloud";
	private static final String nodeDescription = "Code2Cloud builder";

	private String address;
	private C2CSlaveCloud cloud;

	public C2CSlave(String name, C2CSlaveCloud cloud) throws FormException, IOException {
		super(name, nodeDescription, remoteFS, numExecutors, mode, label, new C2CComputerLauncher(cloud.getSshUser(),
				cloud.getSshKeyFilePath()), new C2CRetentionStrategy(), Collections.EMPTY_LIST);
		this.cloud = cloud;
	}

	@Override
	public C2CSlaveComputer createComputer() {
		return new C2CSlaveComputer(this);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
		cloud.returnSlave(this, listener);
	}
}
