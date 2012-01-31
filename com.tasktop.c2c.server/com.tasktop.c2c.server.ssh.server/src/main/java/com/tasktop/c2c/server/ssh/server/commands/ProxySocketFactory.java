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
package com.tasktop.c2c.server.ssh.server.commands;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.LoggerFactory;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class ProxySocketFactory {

	private int receiveBufferSize = -1;

	private int sendBufferSize = -1;

	private int soTimeout = 60 * 1000;

	public Socket openConnection(String host, int port) throws IOException {
		Socket socket = new Socket();
		try {
			socket.setKeepAlive(false);
			if (receiveBufferSize > 0) {
				socket.setReceiveBufferSize(receiveBufferSize);
			}
			if (sendBufferSize > 0) {
				socket.setSendBufferSize(sendBufferSize);
			}
			socket.setSoTimeout(soTimeout);
			socket.connect(new InetSocketAddress(host, port));
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass().getName()).error(
					"Cannot open socket: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			try {
				socket.close();
			} catch (IOException e1) {
				// ignore
			}
			throw e;
		}
		return socket;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

}
