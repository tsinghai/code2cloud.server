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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tasktop.c2c.server.cloud.domain.ServiceHost;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.Session;

public class SshNodeCleaningService implements NodeCleaningService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SshNodeCleaningService.class.getSimpleName());

	private String sshUser;
	private String sshKeyFilePath;
	private List<String> directoriesToClean;

	public void cleanNode(ServiceHost node) throws IOException {
		Connection conn = new Connection(node.getInternalNetworkAddress(), 22);
		// FIXME just accepting host key blindly,
		// hoping that no man-in-the-middle attack is going on.
		conn.connect(new ServerHostKeyVerifier() {
			public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm,
					byte[] serverHostKey) throws Exception {
				return true;
			}
		});
		conn.authenticateWithPublicKey(this.sshUser, new File(this.sshKeyFilePath), null);

		for (String directory : directoriesToClean) {
			Session session = conn.openSession();
			session.execCommand("rm -rf " + directory);
			session.waitForCondition(ChannelCondition.EXIT_SIGNAL, 0);
		}

		// This will kill all of the users process, including the current ssh connection!
		String killProcCommand = "kill -9 -1";

		try {
			Session session = conn.openSession();
			session.execCommand(killProcCommand); // Expect to trigger an IOEception
			LOGGER.warn("Expected an exception while killing all procces");
			session.waitForCondition(ChannelCondition.EXIT_SIGNAL, 0);

			conn.close();
		} catch (IOException e) {
			// Expected
		}

	}

	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}

	public void setSshKeyFilePath(String sshKeyFilePath) {
		this.sshKeyFilePath = sshKeyFilePath;
	}

	public void setDirectoriesToClean(List<String> directoriesToClean) {
		this.directoriesToClean = directoriesToClean;
	}

	public void setDirectoriesToCleanString(String commaSeperatedDirectores) {
		String[] dirs = commaSeperatedDirectores.split(",");
		this.directoriesToClean = Arrays.asList(dirs);
	}

}
