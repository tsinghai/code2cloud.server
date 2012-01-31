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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.internal.tasks.domain.Comment;
import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.internal.tasks.service.TaskActivityService;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockCommentFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProductFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockTaskFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class TaskActivityServiceTest {

	@Autowired
	TaskActivityService activityService;

	@PersistenceContext(unitName = "tasksDomain")
	protected EntityManager entityManager;

	private Product product;

	private Profile profile;

	private Task managedTask;

	private Task modifiedTask;

	@Before
	public void before() {
		product = MockProductFactory.create(entityManager);
		profile = MockProfileFactory.create(entityManager);
		managedTask = MockTaskFactory.create(entityManager, product, profile);
		MockCommentFactory.create(entityManager, managedTask, managedTask.getReporter(), 1);

		entityManager.flush();

		modifiedTask = managedTask;
		entityManager.detach(modifiedTask);
		entityManager.detach(modifiedTask.getComments().get(0));

		managedTask = entityManager.find(Task.class, managedTask.getId());

		modifiedTask.setDeltaTs(new Date());

		assertNoActivity();
	}

	private List<TaskActivity> computeActivity() {
		return entityManager.createQuery("select e from " + TaskActivity.class.getSimpleName() + " as e")
				.getResultList();
	}

	private void assertNoActivity() {
		List<TaskActivity> activity = computeActivity();
		assertEquals("Expected no activity, but found " + activity, 0, activity.size());
	}

	private void assertActivity(String fieldName, Object removed, Object added) {
		assertActivity(fieldName, removed == null ? null : removed.toString(), added == null ? null : added.toString());
	}

	private void assertActivity(String fieldName, String removed, String added) {
		if (removed == null) {
			removed = "";
		}
		if (added == null) {
			added = "";
		}

		List<TaskActivity> activity = computeActivity();
		assertEquals("Expected 1 activity, but found " + activity, 1, activity.size());
		TaskActivity taskActivity = activity.get(0);
		assertEquals(fieldName, taskActivity.getFielddefs().getName());
		assertEquals(removed, taskActivity.getId().getRemoved());
		assertEquals(added, taskActivity.getId().getAdded());
	}

	@Test
	public void testNoActivity() throws EntityNotFoundException {
		recordActivity();

		assertNoActivity();
	}

	private void recordActivity() {
		try {
			activityService.recordActivity(managedTask, null, modifiedTask, null, profile, modifiedTask.getDeltaTs());
		} catch (EntityNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void testSummaryChanged() {
		modifiedTask.setShortDesc(modifiedTask.getShortDesc() + " modified");

		recordActivity();

		assertActivity("short_desc", managedTask.getShortDesc(), modifiedTask.getShortDesc());
	}

	@Test
	public void testVersionChanged() {
		modifiedTask.setVersion(modifiedTask.getVersion() + " m");

		recordActivity();

		assertActivity("version", managedTask.getVersion(), modifiedTask.getVersion());
	}

	@Test
	public void testAssigneeChanged() {
		modifiedTask.setAssignee(MockProfileFactory.create(entityManager));

		recordActivity();

		assertActivity("assigned_to", managedTask.getAssignee().getLoginName(), modifiedTask.getAssignee()
				.getLoginName());
	}

	@Test
	public void testProductChanged() {
		modifiedTask.setProduct(MockProductFactory.create(entityManager));

		recordActivity();

		assertActivity("product", managedTask.getProduct().getName(), modifiedTask.getProduct().getName());
	}

	@Test
	public void testComponentChanged() {
		Component newComponent = MockProductFactory.create(entityManager).getComponents().iterator().next();
		modifiedTask.setComponent(newComponent);

		recordActivity();

		assertActivity("component", managedTask.getComponent().getName(), modifiedTask.getComponent().getName());
	}

	@Ignore
	@Test
	public void testBugFileLocChanged() {
		modifiedTask.setBugFileLoc("http://example.com/" + System.currentTimeMillis());

		recordActivity();

		assertActivity("bug_file_loc", managedTask.getBugFileLoc(), modifiedTask.getBugFileLoc());
	}

	@Test
	public void testSeverityChanged() {
		modifiedTask.setSeverity("s" + System.currentTimeMillis());

		recordActivity();

		assertActivity("bug_severity", managedTask.getSeverity(), modifiedTask.getSeverity());
	}

	@Test
	public void testStatusChanged() {
		modifiedTask.setStatus("s" + System.currentTimeMillis());

		recordActivity();

		assertActivity("bug_status", managedTask.getStatus(), modifiedTask.getStatus());
	}

	@Ignore
	@Test
	public void testOpSysChanged() {
		modifiedTask.setOpSys("Super Operating System 3");

		recordActivity();

		assertActivity("op_sys", managedTask.getOpSys(), modifiedTask.getOpSys());
	}

	@Test
	public void testPriorityChanged() {
		modifiedTask.setPriority("p" + System.currentTimeMillis());

		recordActivity();

		assertActivity("priority", managedTask.getPriority(), modifiedTask.getPriority());
	}

	@Ignore
	@Test
	public void testRepPlatform() {
		modifiedTask.setRepPlatform("platform " + System.currentTimeMillis());

		recordActivity();

		assertActivity("rep_platform", managedTask.getRepPlatform(), modifiedTask.getRepPlatform());
	}

	@Test
	public void testResolution() {
		modifiedTask.setResolution("ALMOST_FIXED");

		recordActivity();

		assertActivity("resolution", managedTask.getResolution(), modifiedTask.getResolution());
	}

	@Test
	public void testTargetMilestone() {
		modifiedTask.setTargetMilestone("t" + System.currentTimeMillis());

		recordActivity();

		assertActivity("target_milestone", managedTask.getTargetMilestone(), modifiedTask.getTargetMilestone());
	}

	@Test
	public void testKeywords() {
		modifiedTask.setKeywords(modifiedTask.getKeywords() + " kw" + System.currentTimeMillis());

		recordActivity();

		assertActivity("keywords", managedTask.getKeywords(), modifiedTask.getKeywords());
	}

	@Test
	public void testEstimatedTime() {
		modifiedTask.setEstimatedTime(new BigDecimal(100));

		recordActivity();

		assertActivity("estimated_time", managedTask.getEstimatedTime(), modifiedTask.getEstimatedTime());
	}

	@Test
	public void testRemainingTime() {
		modifiedTask.setRemainingTime(new BigDecimal(100));

		recordActivity();

		assertActivity("remaining_time", managedTask.getRemainingTime(), modifiedTask.getRemainingTime());
	}

	@Test
	public void testDeadline() {
		modifiedTask.setDeadline(new Date(System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 13L)));

		recordActivity();

		assertActivity("deadline", managedTask.getDeadline(), modifiedTask.getDeadline());
	}

	@Test
	public void testAlias() {
		modifiedTask.setAlias("a" + System.currentTimeMillis());

		recordActivity();

		assertActivity("alias", managedTask.getAlias(), modifiedTask.getAlias());
	}

	@Test
	public void testDescription() {
		Comment modifiedComment = modifiedTask.getComments().get(0);
		modifiedComment.setThetext("more text" + System.currentTimeMillis());

		recordActivity();

		Comment originalComment = managedTask.getComments().get(0);
		assertActivity("longdesc", originalComment.getThetext(), modifiedComment.getThetext());
	}

	@Test
	public void testDescriptionWithLongUtf8Encoding() throws UnsupportedEncodingException {
		char longEncodingCharacter = 0x2019; // ’

		// sanity: we need a character that has > 1 utf-8 encoding
		assertEquals(3, ("" + longEncodingCharacter).getBytes("utf-8").length);

		String sampleData = "abc";
		while (sampleData.length() < 255) {
			sampleData += longEncodingCharacter;
		}
		assertEquals(255, sampleData.length());
		// sanity: our test data is > 255 bytes when utf-8 encoded
		assertTrue(sampleData.getBytes("utf-8").length > 255);

		Comment modifiedComment = modifiedTask.getComments().get(0);
		modifiedComment.setThetext(sampleData);

		recordActivity();

		Comment originalComment = managedTask.getComments().get(0);

		List<TaskActivity> activity = computeActivity();
		assertEquals("Expected 1 activity, but found " + activity, 1, activity.size());
		TaskActivity taskActivity = activity.get(0);

		assertEquals(originalComment.getThetext(), taskActivity.getId().getRemoved());
		String added = taskActivity.getId().getAdded();
		assertTrue(added.endsWith("..."));

		String addedWithoutEllipses = added.substring(0, added.length() - 3);
		assertTrue(modifiedComment.getThetext().startsWith(addedWithoutEllipses));

		assertTrue(added.length() < 255);
		int encodedBytes = added.getBytes("utf-8").length;
		assertTrue(encodedBytes >= 254 && encodedBytes <= 255);
	}

	@Test
	public void testCcsAdded() {
		Profile cc = MockProfileFactory.create(entityManager);
		entityManager.flush();
		entityManager.detach(cc);
		modifiedTask.addCc(cc);

		recordActivity();

		assertActivity("cc", "", cc.getLoginName());
	}

	@Test
	public void testCcsRemoved() {
		Profile cc = MockProfileFactory.create(entityManager);
		entityManager.flush();
		entityManager.detach(managedTask);
		entityManager.detach(cc);
		managedTask.addCc(cc);

		recordActivity();

		assertActivity("cc", cc.getLoginName(), "");
	}
}
