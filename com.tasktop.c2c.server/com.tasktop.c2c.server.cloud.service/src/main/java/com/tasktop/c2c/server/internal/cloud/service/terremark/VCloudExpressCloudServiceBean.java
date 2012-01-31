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
package com.tasktop.c2c.server.internal.cloud.service.terremark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ComputeType;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.jclouds.trmk.vcloud_0_8.domain.Task;
import org.jclouds.trmk.vcloud_0_8.domain.TaskStatus;
import org.jclouds.trmk.vcloud_0_8.domain.VApp;
import org.jclouds.trmk.vcloud_0_8.domain.VAppConfiguration;
import org.jclouds.trmk.vcloud_0_8.domain.VDC;
import org.jclouds.trmk.vcloud_0_8.options.CloneVAppOptions;
import org.jclouds.trmk.vcloudexpress.TerremarkVCloudExpressClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.tasktop.c2c.server.cloud.domain.Identifiable;
import com.tasktop.c2c.server.cloud.domain.Node;
import com.tasktop.c2c.server.cloud.domain.Node.Status;
import com.tasktop.c2c.server.cloud.service.CloudService;
import com.tasktop.c2c.server.cloud.service.Template;

@Service("realCloudService")
@Qualifier("main")
public class VCloudExpressCloudServiceBean implements CloudService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VCloudExpressCloudServiceBean.class);

	private static void debug(String msg, Object... args) {
		if (LOGGER.isDebugEnabled()) {
			try {
				LOGGER.debug(String.format(msg, args));
			} catch (IllegalFormatException e) {
				LOGGER.warn("Bad log format: " + e.getMessage());
				LOGGER.debug(msg);
			}
		}
	}

	private static final Predicate<ComputeMetadata> TEMPLATE_PREDICATE = Predicates.and(//
			new ComputeTypePredicate(ComputeType.NODE, ComputeType.IMAGE), // in vCloud Express, templates are just
																			// nodes that aren't started.
			new NamePredicate(Pattern.compile("template-.+", Pattern.CASE_INSENSITIVE)) // by convention, all templates
																						// have 'template-' prefix
			);

	private static final Predicate<ComputeMetadata> NODE_PREDICATE = new ComputeTypePredicate(ComputeType.NODE);

	private static final String TRMK_VCLOUDEXPRESS = "trmk-vcloudexpress";

	@Autowired
	private VCloudExpressConfiguration configuration;

	@Autowired
	private ComputeServiceContextFactory computeServiceContextFactory;

	public VCloudExpressConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(VCloudExpressConfiguration configuration) {
		this.configuration = configuration;
	}

	public ComputeServiceContextFactory getComputeServiceContextFactory() {
		return computeServiceContextFactory;
	}

	public void setComputeServiceContextFactory(ComputeServiceContextFactory computeServiceContextFactory) {
		this.computeServiceContextFactory = computeServiceContextFactory;
	}

	protected static final class ComputeTypePredicate implements Predicate<ComputeMetadata> {
		private Set<ComputeType> validTypes = new HashSet<ComputeType>();

		public ComputeTypePredicate(ComputeType... types) {
			for (ComputeType type : types) {
				validTypes.add(type);
			}
		}

		@Override
		public boolean apply(ComputeMetadata meta) {
			return validTypes.contains(meta.getType());
		}
	}

	protected static final class NamePredicate implements Predicate<ComputeMetadata> {

		private final Pattern pattern;

		public NamePredicate(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean apply(ComputeMetadata meta) {
			String name = meta.getName();
			return name != null && pattern.matcher(name).matches();
		}
	}

	protected interface ContextOperation<T> {
		public T perform(ComputeServiceContext context);
	}

	protected abstract class ComputeServiceOperation<T> implements ContextOperation<T> {
		@Override
		public T perform(ComputeServiceContext context) {
			ComputeService computeService = context.getComputeService();
			return perform(computeService);
		}

		protected abstract T perform(ComputeService computeService);
	}

	protected abstract class TerramarkVCloudExpressOperation<T> implements ContextOperation<T> {
		@Override
		public T perform(ComputeServiceContext context) {
			TerremarkVCloudExpressClient vCloudExpressClient = TerremarkVCloudExpressClient.class.cast(context
					.getProviderSpecificContext().getApi());
			return perform(vCloudExpressClient);
		}

		protected abstract T perform(TerremarkVCloudExpressClient client);
	}

	protected <T> T perform(ContextOperation<T> operation) {
		ComputeServiceContext context = computeServiceContextFactory.createContext(TRMK_VCLOUDEXPRESS,
				configuration.getIdentity(), configuration.getPassword(),
				ImmutableSet.<Module> of(new JschSshClientModule()));
		try {
			return operation.perform(context);
		} finally {
			context.close();
		}
	}

	// perform(new TerramarkVCloudExpressOperation<Object>() {
	//
	// @Override
	// protected Object perform(TerremarkVCloudExpressClient client) {
	// TerremarkOrg org = client.findOrgNamed(null);
	// System.out.println("default org: " + org);
	// VDC vdc = client.findVDCInOrgNamed(null, null);
	// System.out.println("default vdc: " + vdc);
	// for (Entry<String, ReferenceType> entity : vdc.getResourceEntities().entrySet()) {
	// System.out.println(String.format("\t%s=%s", entity.getKey(), entity.getValue()));
	// }
	// return null;
	// }
	//
	// });
	// return null;
	//

	@Override
	public List<Template> listTemplates() {
		return perform(new ComputeServiceOperation<List<Template>>() {

			@Override
			protected List<Template> perform(ComputeService computeService) {

				List<Template> templates = new ArrayList<Template>(10);

				for (ComputeMetadata meta : computeService.listNodesDetailsMatching(TEMPLATE_PREDICATE)) {
					templates.add(createTemplate(meta));
				}
				debug("listTemplates(): [%s]", templates.toString());
				return templates;
			}
		});
	}

	protected Template createTemplate(ComputeMetadata meta) {
		Template template = new Template();
		configureIdentity(template, meta);
		return template;
	}

	protected Node createNode(NodeMetadata meta) {
		Node node = new Node();
		configureIdentity(node, meta);
		Set<String> addresses = meta.getPrivateAddresses();
		if (!addresses.isEmpty()) {
			node.setIpAddress(addresses.iterator().next());
		}
		node.setStatus(computeStatus(meta.getState()));
		return node;
	}

	private Status computeStatus(NodeState state) {
		if (state != null) {
			switch (state) {
			case PENDING:
				return Status.TRANSITION;
			case RUNNING:
				return Status.RUNNING;
			case SUSPENDED:
				return Status.STOPPED;
			case TERMINATED:
				return Status.DELETED;
			case UNRECOGNIZED:
				return Status.UNKNOWN;
			case ERROR:
				return Status.UNKNOWN;
			}
		}
		return Status.UNKNOWN;
	}

	private void configureIdentity(Identifiable identifiable, ComputeMetadata meta) {
		identifiable.setIdentity(meta.getId());
		identifiable.setUri(meta.getUri());
		identifiable.setName(meta.getName());
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.Task createNode(final Template template, final String newName) {
		return perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				VDC vdc = client.findVDCInOrgNamed(null, null);
				CloneVAppOptions options = new CloneVAppOptions().deploy();
				Task task = client.cloneVAppInVDC(vdc.getHref(), template.getUri(), newName, options);
				com.tasktop.c2c.server.cloud.domain.Task result = createTask(task);
				debug("Create result [%s]", result.toString());
				return result;
			}
		});
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.Task startNode(final Node node) {
		return perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				com.tasktop.c2c.server.cloud.domain.Task result = createTask(client.powerOnVApp(node.getUri()));
				debug("Start result [%s]", result.toString());
				return result;
			}
		});
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.Task allocateDisk(final Node node, final int kilobytes) {
		return perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				VApp vApp = client.getVApp(node.getUri());
				VAppConfiguration appConfig = new VAppConfiguration().addDisk(kilobytes);
				Task task = client.configureVApp(vApp, appConfig);
				com.tasktop.c2c.server.cloud.domain.Task result = createTask(task);
				debug("Allocate disk result [%s]", result.toString());
				return result;
			}
		});
	}

	protected com.tasktop.c2c.server.cloud.domain.Task createTask(Task vcTask) {
		com.tasktop.c2c.server.cloud.domain.Task task = new com.tasktop.c2c.server.cloud.domain.Task();
		task.setUri(vcTask.getHref());
		task.setName(vcTask.getName());
		task.setOperation(vcTask.getOperation());
		task.setStatus(computeStatus(vcTask.getStatus()));
		return task;
	}

	private com.tasktop.c2c.server.cloud.domain.Task.Status computeStatus(TaskStatus status) {
		if (status != null) {
			switch (status) {
			case CANCELLED:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.CANCELLED;
			case ERROR:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.ERROR;
			case QUEUED:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.QUEUED;
			case RUNNING:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.RUNNING;
			case SUCCESS:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.COMPLETE;
			case UNRECOGNIZED:
				return com.tasktop.c2c.server.cloud.domain.Task.Status.UNKNOWN;
			}
		}
		return com.tasktop.c2c.server.cloud.domain.Task.Status.UNKNOWN;
	}

	@Override
	public Node retrieveNode(final String nodeIdentity) {
		return perform(new ComputeServiceOperation<Node>() {

			@Override
			protected Node perform(ComputeService computeService) {
				NodeMetadata nodeMetadata = computeService.getNodeMetadata(nodeIdentity);
				if (nodeMetadata != null) {
					return createNode(nodeMetadata);
				}
				return null;
			}
		});
	}

	@Override
	public Node retrieveNodeByName(final String nodeName) {
		return perform(new ComputeServiceOperation<Node>() {

			@Override
			protected Node perform(ComputeService computeService) {
				Predicate<ComputeMetadata> andPred = Predicates.and(NODE_PREDICATE,
						new NamePredicate(Pattern.compile(Pattern.quote(nodeName))));
				Set<? extends NodeMetadata> nodes = computeService.listNodesDetailsMatching(andPred);
				if (nodes.isEmpty()) {
					return null;
				}
				if (nodes.size() > 1) {
					throw new IllegalStateException("Multiple nodes with name: " + nodeName);
				}
				NodeMetadata nodeMetadata = computeService.getNodeMetadata(nodes.iterator().next().getId());
				Node result = createNode(nodeMetadata);
				debug("retriveNodeByName result [%s]", result);
				return result;
			}
		});
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.Task retrieveTask(
			final com.tasktop.c2c.server.cloud.domain.Task task) {
		return perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				Task vcTask = client.getTask(task.getUri());
				if (vcTask != null) {
					com.tasktop.c2c.server.cloud.domain.Task result = createTask(vcTask);
					debug("retriveTask result [%s]", result.toString());
					return result;
				}
				return null;
			}
		});
	}

	@Override
	public List<Node> listNodes() {
		return perform(new ComputeServiceOperation<List<Node>>() {

			@Override
			protected List<Node> perform(ComputeService computeService) {

				List<Node> nodes = new ArrayList<Node>();

				for (ComputeMetadata meta : computeService.listNodes()) {
					NodeMetadata nodeMeta = computeService.getNodeMetadata(meta.getId());
					nodes.add(createNode(nodeMeta));
				}
				return nodes;
			}
		});
	}

	@Override
	public com.tasktop.c2c.server.cloud.domain.Task stopNode(final Node node) {
		return perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				com.tasktop.c2c.server.cloud.domain.Task result = createTask(client.powerOffVApp(node.getUri()));
				debug("Stop result [%s]", result.toString());
				return result;
			}
		});

	}

	@Override
	public void deleteNode(final Node node) {
		perform(new TerramarkVCloudExpressOperation<com.tasktop.c2c.server.cloud.domain.Task>() {
			@Override
			protected com.tasktop.c2c.server.cloud.domain.Task perform(TerremarkVCloudExpressClient client) {
				client.deleteNode(node.getUri());
				return null;
			}
		});

	}

	// FIXME not efficient
	@Override
	public Node findNodeForIp(String ip) {
		for (Node node : listNodes()) {
			if (node.getIpAddress().equals(ip)) {
				return node;
			}
		}
		return null;
	}
}
