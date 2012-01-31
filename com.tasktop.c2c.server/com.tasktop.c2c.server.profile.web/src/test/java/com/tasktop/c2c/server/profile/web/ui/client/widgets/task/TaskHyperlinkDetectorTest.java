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
package com.tasktop.c2c.server.profile.web.ui.client.widgets.task;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.Hyperlink;
import com.tasktop.c2c.server.tasks.client.widgets.TaskHyperlinkDetector;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class TaskHyperlinkDetectorTest {

	TaskHyperlinkDetector detector = new TaskHyperlinkDetector("test");

	@Test
	public void testNoMatch() {
		Assert.assertTrue(detector.detectHyperlinks("").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("task").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("task ").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("a task or a bug as no links").isEmpty());
	}

	@Test
	public void testSimpleMatch() {
		assertHyperlink("task 123", "task 123", 0, 8);
		assertHyperlink("bug 123", "bug 123", 0, 7);
	}

	@Test
	public void testSimpleMatch2() {
		assertHyperlink(" task 123", "task 123", 1, 8);
		assertHyperlink("task 123 ", "task 123", 0, 8);
	}

	private void assertHyperlink(String text, String uri, int offset, int length) {
		List<Hyperlink> hyperlinks = detector.detectHyperlinks(text);
		for (Hyperlink hyperlink : hyperlinks) {
			if (hyperlink.getUri().equals(uri)) {
				if (hyperlink.getOffset() == offset) {
					if (hyperlink.getLength() == length) {
						return;
					}
				}
			}
		}
		Assert.fail("Expected hyperlink " + uri + " at " + offset + " length " + length + " but got " + hyperlinks);
	}
}
