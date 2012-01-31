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
package com.tasktop.c2c.server.configuration.service;

import java.io.IOException;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceBean.Configurator;


/**
 * Starts the appropriate services.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class ServiceStartingConfigurator implements Configurator {

	private String webserverBinary;

	private String webserverStartCommand = "start";
	private String webserverStopCommand = "stop";

	@Override
	public void configure(NodeConfiguration configuration) {
		// TODO be selective based on the configuration;
		try {
			int stopResult = new ProcessBuilder(webserverBinary, webserverStopCommand).start().waitFor();
			int startResult = new ProcessBuilder(webserverBinary, webserverStartCommand).start().waitFor();
			// TODO, handle incorrect result.
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void setWebserverBinary(String webserverBinary) {
		this.webserverBinary = webserverBinary;
	}

	public void setWebserverStartCommand(String webserverStartCommand) {
		this.webserverStartCommand = webserverStartCommand;
	}

	public void setWebserverStopCommand(String webserverStopCommand) {
		this.webserverStopCommand = webserverStopCommand;
	}

}
