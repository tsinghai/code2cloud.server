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
package com.tasktop.c2c.server.hudson.plugin.cloud;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Listener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.Session;

/**
 * Parts of this taken from EC2UnixLauncher.
 * 
 * 
 */
public class C2CComputerLauncher extends ComputerLauncher {

	private static final String SLAVE_JAR_LOCATION = "/opt/c2c/slave.jar";

	private String sshUser;
	private String sshKeyFilePath;

	public C2CComputerLauncher(String sshUser, String sshKeyFilePath) {
		this.sshUser = sshUser;
		this.sshKeyFilePath = sshKeyFilePath;
	}

	@Override
	public void launch(SlaveComputer _computer, TaskListener listener) {
		try {
			launch((C2CSlaveComputer) _computer, listener.getLogger());
		} catch (IOException e) {
			e.printStackTrace(listener.error(e.getMessage()));
		} catch (InterruptedException e) {
			e.printStackTrace(listener.error(e.getMessage()));
		}
	}

	protected void launch(C2CSlaveComputer computer, PrintStream logger) throws IOException, InterruptedException {
		String address = computer.getNode().getAddress();
		logger.println("Connecting to " + address);
		final Connection conn;
		Connection cleanupConn = null; // java's code path analysis for final doesn't work that well.
		boolean successful = false;

		try {

			conn = connectToSsh(address);
			logger.println("Launching slave agent");
			final Session sess = conn.openSession();
			sess.execCommand("java -jar " + SLAVE_JAR_LOCATION);
			computer.setChannel(sess.getStdout(), sess.getStdin(), logger, new Listener() {
				public void onClosed(Channel channel, IOException cause) {
					sess.close();
					conn.close();
				}
			});
			successful = true;
		} finally {
			if (cleanupConn != null && !successful)
				cleanupConn.close();
		}
	}

	private Connection connectToSsh(String hostName) throws InterruptedException {
		while (true) {
			try {
				Connection conn = new Connection(hostName, 22);
				// FIXME just accepting host key blindly,
				// hoping that no man-in-the-middle attack is going on.
				conn.connect(new ServerHostKeyVerifier() {
					public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm,
							byte[] serverHostKey) throws Exception {
						return true;
					}
				});
				conn.authenticateWithPublicKey(this.sshUser, new File(this.sshKeyFilePath), null);
				return conn; // successfully connected
			} catch (IOException e) {
				// keep retrying until SSH comes up
				Thread.sleep(5000);
			}
		}
	}

	public Descriptor<ComputerLauncher> getDescriptor() {
		throw new UnsupportedOperationException();

	}
}
