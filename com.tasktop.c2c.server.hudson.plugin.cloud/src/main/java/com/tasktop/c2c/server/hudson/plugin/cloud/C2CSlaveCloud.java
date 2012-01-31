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

import hudson.model.Descriptor.FormException;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolService;
import com.tasktop.c2c.server.cloud.service.HudsonSlavePoolServiceClient;
import com.tasktop.c2c.server.cloud.service.RequestBuildSlaveResult;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;

public class C2CSlaveCloud extends Cloud {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(C2CSlaveCloud.class.getName());

	private transient static HudsonSlavePoolServiceClient hudsonSlavePoolService = null;
	private static int nextSlaveNum = 1;

	private String slavePoolServiceBaseUrl = "http://localhost:8088/slavePool";
	private String sshUser;
	private String sshKeyFilePath;
	private String projectIdentifier;
	private static final int SLAVE_LEASE_EXPIRY_RENEW_TIME = 5 * 60 * 1000;
	private static final Timer timer = new Timer();

	protected C2CSlaveCloud(String name) {
		super(name);
	}

	private synchronized HudsonSlavePoolService getSlavePoolService() {
		if (hudsonSlavePoolService == null) {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "classpath:applicationContext-hudsonCloudPlugin.xml" },
					false) {
				@Override
				protected void initBeanDefinitionReader(
						XmlBeanDefinitionReader reader) {
					super.initBeanDefinitionReader(reader);
					reader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(
							C2CSlaveCloud.class.getClassLoader()));
				}
			};
			context.setClassLoader(C2CSlaveCloud.class.getClassLoader());
			context.refresh();
			hudsonSlavePoolService = (HudsonSlavePoolServiceClient) context
					.getBean("hudsonSlavePoolServiceClient");
			hudsonSlavePoolService.setBaseUrl(slavePoolServiceBaseUrl);
			LOGGER.debug("slave pool service initialized");
		}

		return hudsonSlavePoolService;
	}

	@Override
	public Collection<PlannedNode> provision(final Label label,
			int excessWorkload) {
		LOGGER.debug("Provisioning ");
		List<PlannedNode> r = new ArrayList<PlannedNode>();

		for (int i = 0; i < excessWorkload; i++) {
			r.add(new PlannedNode("build slave", Computer.threadPoolForRemoting
					.submit(new Callable<Node>() {
						public Node call() throws Exception {
							// TODO: record the output somewhere
							C2CSlave s = provisionNewNode();
							Hudson.getInstance().addNode(s);
							// Make sure we can connect.
							s.toComputer().connect(false).get();
							LOGGER.debug("Provisioned ");
							return s;
						}
					}), 1));
		}

		return r;
	}

	private C2CSlave provisionNewNode() {
		C2CSlave slave = null;
		try {
			slave = new C2CSlave("Builder " + nextSlaveNum++, this);
		} catch (FormException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String promiseToken = null;
		RequestBuildSlaveResult requestSlaveResult;

		retryLoop: while (true) {
			requestSlaveResult = requestSlaveWithRetry(promiseToken);
			switch (requestSlaveResult.getType()) {
			case PROMISE:
				promiseToken = requestSlaveResult.getPromiseToken();
				sleepWaitForPromise(requestSlaveResult);
				break;
			case SLAVE:
				break retryLoop;
			}
		}

		slave.setAddress(requestSlaveResult.getSlaveIp());
		setRenewLeaseTimer(slave, requestSlaveResult.getSlaveDueDate());
		return slave;
	}

	private void sleepWaitForPromise(RequestBuildSlaveResult requestSlaveResult) {
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private RequestBuildSlaveResult requestSlaveWithRetry(String promiseOrNull) {
		int numFailures = 0;
		int maxFailures = 5;

		while (true) {
			try {
				return getSlavePoolService().acquireSlave(
						getProjectIdentifier(), promiseOrNull);
			} catch (Exception e) {
				promiseOrNull = null;
				numFailures++;
				if (numFailures >= maxFailures) {
					LOGGER.warn("Unable to aquire slave node after ["
							+ numFailures + "] tries.");
					throw new RuntimeException(
							"Unable to aquire slave node after [" + numFailures
									+ "] tries.");
				}
				long sleepTime = (long) Math.pow(10, numFailures + 1);
				LOGGER.info("No slave node available. Sleeping [" + sleepTime
						+ "] before retrying.");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	@Override
	public boolean canProvision(Label label) {
		return true;
	}

	/**
	 * Gets the first {@link C2SSlaveCloud} instance configured in the current
	 * Hudson, or null if no such thing exists.
	 */
	public static C2CSlaveCloud get() {
		return Hudson.getInstance().clouds.get(C2CSlaveCloud.class);
	}

	/**
	 * Try and renew the slaves lease.
	 * 
	 * @param c2cSlave
	 * @return null if the lease can not be removed. date of next expiry if it
	 *         was removed.
	 */
	private Date renewSlaveLeaase(C2CSlave c2cSlave) {
		try {
			RequestBuildSlaveResult result = getSlavePoolService().renewSlave(
					getProjectIdentifier(), c2cSlave.getAddress());
			if (result.getType().equals(RequestBuildSlaveResult.Type.SLAVE)) {
				return result.getSlaveDueDate();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void setRenewLeaseTimer(final C2CSlave slave, Date expirTime) {
		if (expirTime == null) {
			return;
		}
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				onLeaseAboutToExpire(slave);

			}
		}, new Date(expirTime.getTime() - SLAVE_LEASE_EXPIRY_RENEW_TIME));
	}

	private void onLeaseAboutToExpire(C2CSlave slave) {
		if (!Hudson.getInstance().getNodes().contains(slave)) {
			return; // already terminated
		}
		Date nextExpiry = renewSlaveLeaase(slave);
		if (nextExpiry != null) {
			setRenewLeaseTimer(slave, nextExpiry);
		} else {
			try {
				slave.terminate();
			} catch (InterruptedException e) {
				LOGGER.warn("", e);
			} catch (IOException e) {
				LOGGER.warn("", e);
			}
		}
	}

	public void returnSlave(C2CSlave c2cSlave, TaskListener listener) {
		int numFailures = 0;
		int maxFailures = 10;
		int maxWait = 30 * 1000;
		while (true) {
			try {
				getSlavePoolService().releaseSlave(getProjectIdentifier(),
						c2cSlave.getAddress());
				LOGGER.debug("[" + getProjectIdentifier()
						+ "] released build slave [" + c2cSlave.getAddress()
						+ "].");
				break;
			} catch (InsufficientPermissionsException e) {
				// We had lost ownership of this slave. (build timeout, manual
				// reclamiation..)
				LOGGER.debug("[" + getProjectIdentifier()
						+ "] released build slave [" + c2cSlave.getAddress()
						+ "] due to IPE (we did not own it).");
				return;
			} catch (RuntimeException e) { // Client side will throw for
											// connectivity issues

				// Failure
				numFailures++;
				if (numFailures > maxFailures) {
					LOGGER.info("[" + getProjectIdentifier()
							+ "] release of slave failed. Giving up.", e);
					return;
				}
				long sleepTime = (long) Math.pow(10, numFailures + 1);
				if (sleepTime > maxWait) {
					sleepTime = maxWait;
				}
				LOGGER.info("[" + getProjectIdentifier()
						+ "] release of slave failed. Sleeping [" + sleepTime
						+ "] before retrying.", e);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e);
				}
			}
		}

		listener.getLogger().append(
				"[" + getProjectIdentifier() + "] released build slave ["
						+ c2cSlave.getAddress() + "].");
	}

	public String getSlavePoolServiceBaseUrl() {
		return slavePoolServiceBaseUrl;
	}

	public void setSlavePoolServiceBaseUrl(String slavePoolServiceBaseUrl) {
		this.slavePoolServiceBaseUrl = slavePoolServiceBaseUrl;
	}

	public String getSshUser() {
		return sshUser;
	}

	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}

	public String getSshKeyFilePath() {
		return sshKeyFilePath;
	}

	public void setSshKeyFilePath(String sshKeyFilePath) {
		this.sshKeyFilePath = sshKeyFilePath;
	}

	public String getProjectIdentifier() {
		if (projectIdentifier == null) {
			// Try to parse out the projectIdentifier from the hudson url
			try {
				URI rootUri = new URI(Hudson.getInstance().getRootUrl());
				Matcher m = Pattern.compile("/s/([^/]+)/hudson/").matcher(
						rootUri.getPath());
				if (m.matches()) {
					projectIdentifier = m.group(1);
				}

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return projectIdentifier;
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}

}
