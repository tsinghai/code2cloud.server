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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;

/**
 * Goes through the template files and replaces entries of the form ${foo.bar} with the corresponding key from the
 * configuration properties. If the key is not found, then we ignore the replacement (rather than throwing an
 * exception).
 * 
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public class TemplateProcessingConfigurator implements NodeConfigurationServiceBean.Configurator {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateProcessingConfigurator.class.getName());

	private String templateBaseLocation;
	private String targetBaseLocation;

	@Override
	public void configure(NodeConfiguration configuration) {

		File hudsonHomeDir = new File(targetBaseLocation, configuration.getApplicationId());

		if (hudsonHomeDir.exists()) {
			LOG.warn("Hudson home already apears to exist: " + hudsonHomeDir.getAbsolutePath());
		} else {
			// If we're here, the destination directory doesn't exist. Create it now.
			LOG.info("Creating new Hudson home: " + hudsonHomeDir.getAbsolutePath());
			hudsonHomeDir.mkdirs();
		}

		Queue<File> fileQueue = new LinkedList<File>();
		Map<String, String> props = new HashMap<String, String>(configuration.getProperties());

		fileQueue.add(new File(templateBaseLocation));
		while (!fileQueue.isEmpty()) {
			File currentFile = fileQueue.poll();
			if (currentFile.isDirectory()) {
				fileQueue.addAll(Arrays.asList(currentFile.listFiles()));
				createOrEnsureTargetDirectory(currentFile, configuration);
			} else {
				try {
					applyTemplateFileToTarget(props, currentFile, configuration);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	// FIXME this is not efficient
	private void applyTemplateFileToTarget(Map<String, String> props, File templateFile, NodeConfiguration configuration)
			throws IOException {

		String targetContents = FileUtils.readFileToString(templateFile);
		for (Entry<String, String> entry : props.entrySet()) {
			targetContents = targetContents.replace("${" + entry.getKey() + "}", entry.getValue());
		}

		File targetFile = mapToTargetFile(templateFile, configuration);
		FileUtils.writeStringToFile(targetFile, targetContents);
	}

	private void createOrEnsureTargetDirectory(File templateDirectory, NodeConfiguration configuration) {
		File targetDirectory = mapToTargetFile(templateDirectory, configuration);
		if (!targetDirectory.exists()) {
			if (!targetDirectory.mkdir()) {
				throw new IllegalStateException("could not create targetDirectory ["
						+ targetDirectory.getAbsolutePath() + "]");
			}
		} else if (!targetDirectory.isDirectory()) {
			throw new IllegalStateException("targetDirectory [" + targetDirectory.getAbsolutePath()
					+ "] is not a directory");
		}
		// Create automatically for template files;
	}

	private File mapToTargetFile(File templateFile, NodeConfiguration configuration) {
		String templateFilePath = templateFile.getAbsolutePath().replace("C:", "");
		templateFilePath = templateFilePath.replace("\\", "/");
		int startOfBase = templateFilePath.indexOf(templateBaseLocation);
		if (startOfBase != 0) {
			throw new IllegalStateException();
		}

		String fullTargetBaseLocation = targetBaseLocation;
		if (configuration.getApplicationId() != null) {
			fullTargetBaseLocation = fullTargetBaseLocation.concat("/" + configuration.getApplicationId());
		}
		String filename = fullTargetBaseLocation + templateFilePath.substring(templateBaseLocation.length());
		return new File(filename);
	}

	public void setTemplateBaseLocation(String templateBaseLocation) {
		this.templateBaseLocation = templateBaseLocation;
	}

	public void setTargetBaseLocation(String targetBaseLocation) {
		this.targetBaseLocation = targetBaseLocation;
	}
}
