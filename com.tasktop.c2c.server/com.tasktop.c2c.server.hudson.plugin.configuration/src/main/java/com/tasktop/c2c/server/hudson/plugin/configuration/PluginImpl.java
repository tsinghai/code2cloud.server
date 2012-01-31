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
package com.tasktop.c2c.server.hudson.plugin.configuration;
import hudson.BulkChange;
import hudson.Extension;
import hudson.Plugin;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import java.io.IOException;

@Extension
public class PluginImpl extends Plugin implements Describable<PluginImpl> {

	@Initializer(after = InitMilestone.STARTED)
	public synchronized static void updateHudsonConfig() throws IOException {

		Hudson hudson = Hudson.getInstance();
		BulkChange bc = new BulkChange(hudson);
		try {

			// update the hudson configuration
			hudson.setNumExecutors(0);

			hudson.save();
		} finally {
			bc.commit();
		}
	}

	public void start() throws Exception {
		load();
	}

	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(getClass());
	}

	public static PluginImpl get() {
		return Hudson.getInstance().getPlugin(PluginImpl.class);
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<PluginImpl> {
		@Override
		public String getDisplayName() {
			return "Code2Cloud Configuration Plugin";
		}
	}
}
