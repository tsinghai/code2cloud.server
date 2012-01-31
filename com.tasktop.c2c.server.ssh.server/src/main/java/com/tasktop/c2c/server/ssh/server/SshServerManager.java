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
package com.tasktop.c2c.server.ssh.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sshd.common.KeyPairProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
@Component
public class SshServerManager implements Ordered, InitializingBean, DisposableBean {

	@Autowired(required = true)
	private org.apache.sshd.SshServer sshServer;

	public org.apache.sshd.SshServer getSshServer() {
		return sshServer;
	}

	public void setSshServer(org.apache.sshd.SshServer sshServer) {
		this.sshServer = sshServer;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		sanityCheckKeypairs();
		sshServer.start();
	}

	private void sanityCheckKeypairs() {
		// sanity check: we should have some key pairs
		KeyPairProvider keyPairProvider = sshServer.getKeyPairProvider();
		String[] keyTypes = keyPairProvider.getKeyTypes().trim().split("\\s*,\\s*");
		if (keyTypes.length == 0 || keyTypes[0].isEmpty()) {
			throw new IllegalStateException("No keypairs are available for SSH: " + keyTypes);
		} else {
			LoggerFactory.getLogger(SshServerManager.class.getName()).info(
					"SSH keypair types: " + keyPairProvider.getKeyTypes());
		}
	}

	@Override
	public void destroy() throws Exception {
		sshServer.stop();
	}
}
