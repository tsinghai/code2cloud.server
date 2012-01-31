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

import java.net.InetAddress;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.cloud.domain.Node;


@Component
public class IsReachableNodeReadyStrategy implements NodeReadyStrategy {

	@Override
	public boolean isNodeReady(Node node) {
		return isReachable(node.getIpAddress());
	}

	boolean isReachable(String ip) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			return addr.isReachable(200);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		IsReachableNodeReadyStrategy s = new IsReachableNodeReadyStrategy();
		boolean reachable = s.isReachable(args[0]);
		System.out.println(args[0] + " is " + (reachable ? "reachable" : "not reachable"));
	}
}
