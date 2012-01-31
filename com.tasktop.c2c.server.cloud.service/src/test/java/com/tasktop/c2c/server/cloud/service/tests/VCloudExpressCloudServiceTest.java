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

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.domain.Credentials;
import org.jclouds.rest.RestContext;
import org.jclouds.trmk.vcloud_0_8.domain.TaskStatus;
import org.jclouds.trmk.vcloud_0_8.domain.VDC;
import org.jclouds.trmk.vcloud_0_8.domain.internal.TaskImpl;
import org.jclouds.trmk.vcloud_0_8.options.CloneVAppOptions;
import org.jclouds.trmk.vcloudexpress.TerremarkVCloudExpressClient;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Predicate;
import com.tasktop.c2c.server.cloud.domain.Task;
import com.tasktop.c2c.server.cloud.service.Template;
import com.tasktop.c2c.server.internal.cloud.service.terremark.VCloudExpressCloudServiceBean;
import com.tasktop.c2c.server.internal.cloud.service.terremark.VCloudExpressConfiguration;

@ContextConfiguration({ "/applicationContext-test.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class VCloudExpressCloudServiceTest {

	protected Mockery context = new JUnit4Mockery() {
		{
			// Enable CGLIB mocking of concrete classes
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	@Autowired
	VCloudExpressConfiguration configuration;

	VCloudExpressCloudServiceBean service;

	ComputeServiceContextFactory mockComputeServiceContextFactory;
	ComputeServiceContext mockComputeContext;
	ComputeService mockComputeService;
	TerremarkVCloudExpressClient mockVCloudExpressClient;
	RestContext mockProviderSpecificContext;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		mockComputeServiceContextFactory = context.mock(ComputeServiceContextFactory.class);
		mockComputeContext = context.mock(ComputeServiceContext.class);
		mockComputeService = context.mock(ComputeService.class);
		mockVCloudExpressClient = context.mock(TerremarkVCloudExpressClient.class);
		mockProviderSpecificContext = context.mock(RestContext.class);
		service = new VCloudExpressCloudServiceBean();
		service.setConfiguration(configuration);
		service.setComputeServiceContextFactory(mockComputeServiceContextFactory);
		context.checking(new Expectations() {
			{
				allowing(mockComputeServiceContextFactory).createContext(with(any(String.class)),
						with(any(String.class)), with(any(String.class)), with(any(Iterable.class)));
				will(returnValue(mockComputeContext));
				allowing(mockComputeContext).getComputeService();
				will(returnValue(mockComputeService));
				oneOf(mockComputeContext).close();
				allowing(mockComputeContext).getProviderSpecificContext();
				will(returnValue(mockProviderSpecificContext));
				allowing(mockProviderSpecificContext).getApi();
				will(returnValue(mockVCloudExpressClient));
			}
		});
	}

	@Test
	public void testListTemplates() {
		final Set<NodeMetadata> nodeMetadata = provideCloudNodes(2, NodeState.RUNNING);
		List<Template> templates = service.listTemplates();
		assertEquals(nodeMetadata.size(), templates.size());

		int index = 0;
		for (NodeMetadata meta : nodeMetadata) {
			Template template = templates.get(index++);
			assertEquals(meta.getId(), template.getIdentity());
			assertEquals(meta.getName(), template.getName());
			assertEquals(meta.getUri(), template.getUri());
		}
	}

	@Test
	public void testCreateNode() throws Exception {

		final Template template = new Template();
		template.setIdentity("t1");
		template.setName("Template 1");
		template.setUri(new URI("urn:test:t1"));

		final VDC vdc = context.mock(VDC.class);
		context.checking(new Expectations() {
			{
				allowing(vdc).getHref();
				will(returnValue(new URI("urn:vdc1")));
			}
		});
		final org.jclouds.trmk.vcloud_0_8.domain.Task createNodeTask = new TaskImpl(new URI("urn:task:cloneNode:t1"),
				"Clone Node", TaskStatus.QUEUED, new Date(), null, null, null, null);

		context.checking(new Expectations() {
			{
				allowing(mockVCloudExpressClient).findVDCInOrgNamed(with(any(String.class)), with(any(String.class)));
				will(returnValue(vdc));
				oneOf(mockVCloudExpressClient).cloneVAppInVDC(with(vdc.getHref()), with(template.getUri()),
						with(any(String.class)), with(any(CloneVAppOptions.class)));
				will(returnValue(createNodeTask));
			}
		});

		Task nodeTask = service.createNode(template, "VApp1");

		assertEquals(createNodeTask.getHref(), nodeTask.getUri());
		assertEquals(createNodeTask.getName(), nodeTask.getName());
		assertEquals(createNodeTask.getOperation(), nodeTask.getOperation());
		assertEquals(Task.Status.QUEUED, nodeTask.getStatus());
	}

	private Set<NodeMetadata> provideCloudNodes(int nodeCount, NodeState state) {
		final Set<NodeMetadata> nodeMetadata = new HashSet<NodeMetadata>();
		nodeMetadata.addAll(createNodeMetadata(nodeCount, state));
		context.checking(new Expectations() {
			{
				oneOf(mockComputeService).listNodesDetailsMatching(with(any(Predicate.class)));
				will(returnValue(nodeMetadata));
			}
		});
		return nodeMetadata;
	}

	private List<NodeMetadata> createNodeMetadata(int count, NodeState state) {
		try {
			List<NodeMetadata> metadata = new ArrayList<NodeMetadata>(count);
			for (int x = 0; x < count; ++x) {
				Map<String, String> userMetadata = new HashMap<String, String>();
				Set<String> publicAddresses = new HashSet<String>();
				Set<String> privateAddresses = new HashSet<String>();
				privateAddresses.add("10.1.2." + x);
				Credentials credentials = new Credentials("root", "pass");
				URI uri = new URI("urn:test:node" + x);
				// FIXME
				// metadata.add(new NodeMetadataImpl("test", "Node" + x, "node" + x, null, uri, userMetadata, null,
				// null,
				// null, null, state, 22, publicAddresses, privateAddresses, "pass", credentials));
			}
			return metadata;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
