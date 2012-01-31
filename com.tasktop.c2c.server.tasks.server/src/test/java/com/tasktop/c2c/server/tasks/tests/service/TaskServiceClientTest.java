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
package com.tasktop.c2c.server.tasks.tests.service;

import static com.tasktop.c2c.server.common.tests.util.Assert.assertMatches;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.auth.service.proxy.AuthenticationTokenSerializer;
import com.tasktop.c2c.server.auth.service.proxy.ProxyPreAuthClientInvocationHandler;
import com.tasktop.c2c.server.auth.service.proxy.RequestHeaders;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.tests.util.TestResourceUtil;
import com.tasktop.c2c.server.common.tests.util.WebApplicationContainerBean;
import com.tasktop.c2c.server.internal.tasks.service.TaskServiceConfiguration;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
import com.tasktop.c2c.server.tasks.tests.util.TestSecurity;

/**
 * Extending TaskServiceTest and overriding super.taskService with our REST client, the effect is that all of the tests
 * in TaskServiceTest are run through the REST interface.
 * 
 */
@ContextConfiguration({ "/applicationContext-testAuthentication.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TaskServiceClientTest extends TaskServiceTest {

	@Autowired
	private TaskServiceClient taskServiceClient;

	@Autowired
	@Qualifier("preAuthenticatedRestTemplate")
	private RestTemplate restTemplate;

	@Autowired
	private WebApplicationContainerBean container;

	private static WebApplicationContainerBean staticContainer;

	@Autowired
	private TaskServiceConfiguration taskServiceConfiguration;

	@Before
	public void beforeTest() {

		doInitialSetup();

		super.taskService = ProxyPreAuthClientInvocationHandler.wrap(taskServiceClient,
				new ProxyPreAuthClientInvocationHandler() {
					@Override
					public AuthenticationToken getAuthenticationToken() {
						AuthenticationServiceUser user = AuthenticationServiceUser.getCurrent();
						return user == null ? null : user.getToken();
					}
				});
	}

	private static boolean isSetup = false;

	private void doInitialSetup() {

		if (isSetup) {
			return;
		}
		isSetup = true;

		taskServiceClient.setRestTemplate(restTemplate);

		container.setWebRoot(TestResourceUtil.computeResourceFolder("src/test/resources/web-roots",
				TaskServiceClientTest.class));
		container.start();
		taskServiceClient.setBaseUrl(container.getBaseUrl() + "tasks");
		taskServiceConfiguration.setWebHost(container.getBaseUrl().replace("http://", ""));

		// Copy the reference to our static variable so we can clean this up at the end.
		staticContainer = container;
	}

	@AfterClass
	public static void afterClass() {
		if (staticContainer != null) {
			staticContainer.stop();
			staticContainer = null;
		}
	}

	@Test
	public void testWebServiceUp() throws IOException, SAXException {
		WebConversation wc = new WebConversation();
		WebRequest req = new GetMethodWebRequest(container.getBaseUrl() + "tasks/summary");
		setAuthentication(wc);
		WebResponse resp = wc.getResponse(req);
		String responseText = resp.getText();
		System.out.println(responseText);

		assertMatches("\\s*\\{\\s*\"taskSummary\":.*", responseText);
	}

	protected void setAuthentication(final WebConversation wc) {
		AuthenticationToken token = TestSecurity.createToken(super.currentUser);
		new AuthenticationTokenSerializer().serialize(new RequestHeaders() {

			@Override
			public void addHeader(String name, String value) {
				wc.setHeaderField(name, value);
			}
		}, token);
	}

	@Test
	public void testGetAttachmentOverURL() throws ValidationException, IOException, SAXException,
			EntityNotFoundException, ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());

		Attachment attachment = new Attachment();
		attachment.setDescription("desc");
		attachment.setFilename("filename");
		attachment.setMimeType("mimetype");
		attachment.setAttachmentData("DATA".getBytes());

		AttachmentHandle attachmentHandle = taskService.saveAttachment(task.getTaskHandle(), attachment);

		Attachment a = taskService.retrieveAttachment(attachmentHandle.getId());

		// We don't go through the proxy
		String url = a.getUrl().replace(
				"alm/s/" + TenancyContextHolder.getContext().getTenant().getIdentity() + "/tasks/", "tasks/");

		WebConversation wc = new WebConversation();
		WebRequest req = new GetMethodWebRequest(url);
		setAuthentication(wc);
		WebResponse resp = wc.getResponse(req);
		String responseText = resp.getText();
		Assert.assertEquals("DATA", responseText);
	}

	@Test(expected = RuntimeException.class)
	@Override
	// This test is overridden because the exception received is different, and
	// it's not possible to look for multiple exception types.
	public void testRetrieveComponentForNullID() throws Exception {

		// This should blow up
		assertNotNull(taskService.retrieveComponent(null));
	}

}
