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

import java.util.List;

import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.Task;


public interface CloudService {

	public List<Template> listTemplates();

	/**
	 * Create a node. Node is not started.
	 * 
	 * @param template
	 * @param newName
	 * @return
	 */
	public Task createNode(Template template, String newName);

	/**
	 * Allocate a disk for a powered down node.
	 * 
	 * @param node
	 * @param sizeInKillobytes
	 *            Must be a multiple of 1048576 KB (= 1 GB) and greater than 25 GB (= 26214400 KB)
	 * @return
	 */
	public Task allocateDisk(Node node, int sizeInKillobytes);

	public Node retrieveNode(String nodeIdentity);

	public Task retrieveTask(Task task);

	public Node retrieveNodeByName(String nodeName);

	public List<Node> listNodes();

	public Task startNode(Node node);

	public Task stopNode(Node node);

	public void deleteNode(Node node);

	public Node findNodeForIp(String ip);

}
