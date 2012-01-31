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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;

/**
 * Stores the properties in a properties files to be used to config other processes (EG task service).
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class PropertyStoringConfigurator implements NodeConfigurationServiceBean.Configurator {

	private String propertiesLocation;

	@Override
	public void configure(NodeConfiguration configuration) {
		File propertiesFile = new File(propertiesLocation);

		// TODO check permissions/existence

		FileWriter writer = null;
		try {
			writer = new FileWriter(propertiesFile);

			for (Entry<String, String> entry : configuration.getProperties().entrySet()) {
				writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
			}

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	public void setPropertiesLocation(String propertiesLocation) {
		this.propertiesLocation = propertiesLocation;
	}

}
