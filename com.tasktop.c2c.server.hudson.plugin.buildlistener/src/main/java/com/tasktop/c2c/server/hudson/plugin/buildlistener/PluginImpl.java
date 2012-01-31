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
package com.tasktop.c2c.server.hudson.plugin.buildlistener;

import hudson.Extension;
import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class PluginImpl extends Plugin implements Describable<PluginImpl> {

	private Configuration config;

	private static final Logger LOGGER = LoggerFactory.getLogger("hudson." + PluginImpl.class.getName());

	@Extension
	public static final BuildEventGeneratingRunListener LISTENER = new BuildEventGeneratingRunListener();

	@Override
	public void start() throws Exception {
		this.config = loadConfiguration();
		LISTENER.setConfiguration(config);
	}

	@Override
	public void postInitialize() throws Exception {
		super.postInitialize();
		config.configureDefaults(); // Hudson.getRootUrl() throws NPE when called in start()
		LOGGER.info(String.format("Using configuration: %s", config.toString()));
	}

	protected Configuration loadConfiguration() throws IOException {
		XmlFile xmlFile = getConfigXml();
		Configuration config = null;
		if (xmlFile.exists()) {
			LOGGER.info(String.format("Configuration file found at: %s", xmlFile.getFile()));
			config = (Configuration) xmlFile.read();
		} else {
			LOGGER.warn(String.format("Configuration file not found: %s", xmlFile.getFile()));
			config = new Configuration();
		}
		return config;
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
			return "Code2Cloud Build Listener Plugin";
		}
	}
}
