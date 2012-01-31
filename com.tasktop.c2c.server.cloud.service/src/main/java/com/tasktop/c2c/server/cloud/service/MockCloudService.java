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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.Task;
import com.tasktop.c2c.server.cloud.domain.Task.Status;


//@Ignore
public class MockCloudService implements CloudService {

	private List<String> templateNames = Arrays.asList("template-scm-01", "template-hs-01", "template-hm-01",
			"template-tsk-01");
	private int nextIP = 1;
	private boolean incrementIps = false;

	@Override
	public List<Template> listTemplates() {
		List<Template> result = new ArrayList<Template>(templateNames.size());
		for (String templateName : templateNames) {
			Template temp = new Template();
			temp.setName(templateName);
			result.add(temp);
		}
		return result;
	}

	private Task createQueuedTask() {
		Task result = new Task();
		result.setStatus(Status.QUEUED);
		return result;
	}

	@Override
	public Task createNode(Template template, String newName) {
		return createQueuedTask();
	}

	@Override
	public Task allocateDisk(Node node, int sizeInKillobytes) {
		return createQueuedTask();
	}

	private Node createNode() {
		Node result = new Node();
		result.setName("MOCK");
		result.setIpAddress("127.0.0." + nextIP);
		if (incrementIps) {
			nextIP++;
		}
		return result;
	}

	@Override
	public Node retrieveNode(String nodeIdentity) {
		return createNode();
	}

	@Override
	public Task retrieveTask(Task task) {
		Task result = createQueuedTask();
		result.setStatus(Status.COMPLETE);
		return result;
	}

	@Override
	public Node retrieveNodeByName(String nodeName) {
		return createNode();
	}

	@Override
	public List<Node> listNodes() {
		return Arrays.asList(createNode());
	}

	@Override
	public Task startNode(Node node) {
		return createQueuedTask();
	}

	@Override
	public Task stopNode(Node node) {
		return createQueuedTask();
	}

	@Override
	public void deleteNode(Node node) {
		// empty
	}

	@Override
	public Node findNodeForIp(String ip) {
		return createNode();
	}

	public void setIncrementIps(boolean incrementIps) {
		this.incrementIps = incrementIps;
	}

}
