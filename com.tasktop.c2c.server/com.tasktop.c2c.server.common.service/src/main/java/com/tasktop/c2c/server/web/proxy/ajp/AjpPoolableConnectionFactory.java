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
package com.tasktop.c2c.server.web.proxy.ajp;

import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AjpPoolableConnectionFactory extends BaseKeyedPoolableObjectFactory {

	private static final Pattern keyPattern = Pattern.compile("([^:]+):(\\d+)");

	private SocketFactory socketFactory = SocketFactory.getDefault();

	private static final Logger log = LoggerFactory.getLogger(AjpPoolableConnectionFactory.class);

	private void debug(String string) {
		if (log.isDebugEnabled()) {
			log.debug(string);
		}
	}

	public static final class Key {
		private int port;
		private String host;

		public Key(String host, int port) {
			this.port = port;
			this.host = host;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}

		@Override
		public String toString() {
			return host + ':' + port;
		}
	}

	boolean keepAlive = true;
	boolean tcpNoDelay = keepAlive;
	int soTimeout = 1000 * 60;

	@Override
	public Object makeObject(Object objectKey) throws Exception {
		Key key = (Key) objectKey;
		String host = key.getHost();
		int port = key.getPort();
		if (port <= 0) {
			port = 8009;
		}

		Socket socket = socketFactory.createSocket(host, port);
		try {
			socket.setTcpNoDelay(tcpNoDelay);
			socket.setSoTimeout(soTimeout);
			socket.setKeepAlive(keepAlive);
		} catch (SocketException e) {
			socket.close();
			throw e;
		}
		debug("Created new socket: " + socket.toString());
		return socket;
	}

	@Override
	public void destroyObject(Object key, Object obj) throws Exception {
		debug("destroying socket: " + obj.toString());
		((Socket) obj).close();
	}

	@Override
	public boolean validateObject(Object key, Object obj) {
		Socket socket = (Socket) obj;
		return socket.isConnected();
	}

	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

}
