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
package com.tasktop.c2c.server.cloud.service.tests;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.Task;
import com.tasktop.c2c.server.cloud.domain.Node.Status;
import com.tasktop.c2c.server.cloud.service.CloudService;
import com.tasktop.c2c.server.cloud.service.NodeReadyStrategy;
import com.tasktop.c2c.server.cloud.service.NodeWithExtraDiskCreationService;
import com.tasktop.c2c.server.cloud.service.Template;


public class Main {

	private static CloudService cloudService;

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		cloudService = new ClassPathXmlApplicationContext("applicationContext-main.xml").getBean("realCloudService",
				CloudService.class);
		try {
			listTemplatesAndNodes();
			// testAllocateRetrieveByNameAndConfigure();
			// testPoolingNodeProvisioningService();
			// cleanupAlmNodes();
			// createNewHudsonSlave();
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	private static void createNewHudsonSlave() {
		NodeWithExtraDiskCreationService creator = new NodeWithExtraDiskCreationService();
		creator.setCloudService(cloudService);
		creator.setNodeReadyStrategy(new NodeReadyStrategy() {

			@Override
			public boolean isNodeReady(Node node) {
				return true;
			}
		});
		creator.setTaskUpdatePeriod(500);
		creator.setTemplateNodeName("template-hs-01");
		String ip = creator.createNewNode();
		System.out.println("Created ip");
	}

	private static void listTemplatesAndNodes() {
		System.out.println("Templates ");
		for (Template template : cloudService.listTemplates()) {
			System.out.println(template.toString());
		}

		System.out.println("Nodes ");
		for (Node node : cloudService.listNodes()) {
			System.out.println(node.toString());
		}
	}

	private static void testAllocateRetrieveByNameAndConfigure() throws InterruptedException {
		Template template = cloudService.listTemplates().get(0);
		String name = generateDnsName();
		Task task = cloudService.createNode(template, name);
		Node newNode = null;

		while (!task.getStatus().isDone()) {
			System.out.println("Clone task pending: " + task.toString());
			Thread.sleep(500);
			task = cloudService.retrieveTask(task);

			newNode = cloudService.retrieveNodeByName(name);
			System.out.println("Node found by name: " + newNode.toString());
		}
		System.out.println("Task done: " + task.toString());

		task = cloudService.allocateDisk(newNode, 26214400);
		while (!task.getStatus().isDone()) {
			System.out.println("Disk allocate task pending: " + task.toString());
			Thread.sleep(500);
			task = cloudService.retrieveTask(task);
		}

		task = cloudService.startNode(newNode);

		while (!task.getStatus().isDone()) {
			System.out.println("Start task pending: " + task.toString());
			Thread.sleep(500);
			task = cloudService.retrieveTask(task);
		}

		newNode = cloudService.retrieveNodeByName(name);
		System.out.println("Node found by name: " + newNode.toString());
	}

	private static String generateDnsName() {
		return ("alm-" + ("" + UUID.randomUUID()).replace("-", "")).substring(0, 15);
	}

	private static void cleanupAlmNodes() {
		Queue<Task> tasks = new LinkedList<Task>();
		for (Node n : cloudService.listNodes()) {
			if (n.getName().startsWith("alm-")) {
				if (n.getStatus().equals(Status.RUNNING)) {
					tasks.add(cloudService.stopNode(n));
				} else {
					cloudService.deleteNode(n);
				}
			}
		}

		while (!tasks.isEmpty()) {
			Task task = cloudService.retrieveTask(tasks.poll());
			if (!task.getStatus().isDone()) {
				tasks.add(task);
			}
		}

		for (Node n : cloudService.listNodes()) {
			if (n.getName().startsWith("alm-")) {
				cloudService.deleteNode(n);
			}
		}
	}
}
