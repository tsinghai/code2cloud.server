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

import hudson.slaves.AbstractCloudComputer;

public class C2CSlaveComputer extends AbstractCloudComputer<C2CSlave> {

	public C2CSlaveComputer(C2CSlave slave) {
		super(slave);

	}

	@Override
	public C2CSlave getNode() {
		return (C2CSlave) super.getNode();
	}

}
