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
package com.tasktop.c2c.server.monitoring.api;

import org.junit.Ignore;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

import com.tasktop.c2c.server.monitoring.domain.SlowResponseEvent;
import com.tasktop.c2c.server.monitoring.service.MonitoringServiceClient;


@Ignore
public class ClientRunnerTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		RestTemplate restTemplate = new ClassPathXmlApplicationContext(
				"META-INF/spring/applicationContext-restClient.xml").getBean(
				"restTemplate",
				RestTemplate.class);
		MonitoringServiceClient client = new MonitoringServiceClient();
		client.setRestTemplate(restTemplate);
		client.setBaseUrl("http://localhost:8888/alm/monitoring/");
		client.afterPropertiesSet();

		SlowResponseEvent mockEvent = new SlowResponseEvent();
		mockEvent.setEventApplication("code2cloud");
		mockEvent.setDurationInMilliseconds(5000);
		mockEvent.setEventDescription("Slow response");
		mockEvent.setEventLabel("GET Foobar.txt");
		mockEvent.setTraceString("1234");

		client.processEvent(mockEvent);

	}
}
