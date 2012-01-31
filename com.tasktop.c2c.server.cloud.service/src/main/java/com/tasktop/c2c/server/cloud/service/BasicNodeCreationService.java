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
package com.tasktop.c2c.server.cloud.service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.Task;


/**
 * Allocation process: creates a node by copying a given template, then start, then check that the node is ready.
 * Subclass can override configureNodeBeforeStarting to EG add disks.
 * 
 */
public class BasicNodeCreationService implements NodeCreationService, NodeLifecycleService {

	private static final int DEFAULT_TASK_UPDATE_PERIOD = 500;
	private static final int MAX_NAME_SIZE = 15;
	protected int taskUpdatePeriod = DEFAULT_TASK_UPDATE_PERIOD;

	protected CloudService cloudService;
	protected String templateNodeName;
	protected Template templateNode;
	protected NodeReadyStrategy nodeReadyStrategy;
	protected String nodeNamePrefix;
	protected Logger logger = LoggerFactory.getLogger(BasicNodeCreationService.class.getName());

	@Override
	public String createNewNode() {
		Node node = createNodeFromTemplate();
		logger.info("Created new node " + node.getName());
		configureNodeBeforeStarting(node);
		Task startTask = cloudService.startNode(node);
		logger.info("Starting new node " + node.getName());
		waitForTask(startTask);
		logger.info("Started node " + node.getName());
		waitForNodeToBeReady(node);
		logger.info("Node is ready " + node.getName());
		return node.getIpAddress();

	}

	protected void configureNodeBeforeStarting(Node node) {
		// empty
	}

	protected Task waitForTask(Task task) {

		while (true) {
			try {
				Thread.sleep(taskUpdatePeriod);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			task = cloudService.retrieveTask(task);
			switch (task.getStatus()) {
			case CANCELLED:
			case ERROR:
			case UNKNOWN:
				throw new RuntimeException("Task [" + task.toString() + "] failed");
			case RUNNING:
			case QUEUED:
				break;
			case COMPLETE:
				return task;
			default:
				throw new IllegalStateException("Unexpected task status: " + task.getStatus());
			}
		}
	}

	protected void waitForNodeToBeReady(Node node) {
		while (!nodeReadyStrategy.isNodeReady(node)) {
			try {
				Thread.sleep(taskUpdatePeriod);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void intializeTemplateNode() {
		for (Template t : cloudService.listTemplates()) {
			if (t.getName().equals(templateNodeName)) {
				templateNode = t;
				break;
			}
		}
		if (templateNode == null) {
			throw new IllegalStateException("Could not find template node named: " + templateNodeName);
		}
	}

	/** Exposed for testing only. */
	public String generateDnsName() {
		return (getNodeNamePrefix() + UUID.randomUUID().toString().replace("-", "")).substring(0, MAX_NAME_SIZE);
	}

	public void setTaskUpdatePeriod(int taskUpdatePeriod) {
		this.taskUpdatePeriod = taskUpdatePeriod;
	}

	public void setCloudService(CloudService cloudService) {
		this.cloudService = cloudService;
	}

	public void setTemplateNodeName(String templateNodeName) {
		this.templateNodeName = templateNodeName;
	}

	public void setNodeReadyStrategy(NodeReadyStrategy nodeReadyStrategy) {
		this.nodeReadyStrategy = nodeReadyStrategy;
	}

	protected Template getTemplateNode() {
		if (templateNode == null) {
			intializeTemplateNode();
		}
		return templateNode;
	}

	protected Node createNodeFromTemplate() {
		String newName = generateDnsName();

		Task creationTask = cloudService.createNode(getTemplateNode(), newName);
		creationTask = waitForTask(creationTask);
		Node node = cloudService.retrieveNodeByName(newName);
		return node;
	}

	@Override
	public void decomissionNode(String ip) {
		Node node = cloudService.findNodeForIp(ip);
		waitForTask(cloudService.stopNode(node));
		cloudService.deleteNode(node);
	}

	public void setNodeNamePrefix(String nodeNamePrefix) {
		this.nodeNamePrefix = nodeNamePrefix;
	}

	public String getNodeNamePrefix() {
		return nodeNamePrefix;
	}

}
