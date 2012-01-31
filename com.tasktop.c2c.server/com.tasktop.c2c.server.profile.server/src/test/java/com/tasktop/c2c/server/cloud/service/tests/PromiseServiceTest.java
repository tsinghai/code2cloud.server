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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.service.PromiseService;
import com.tasktop.c2c.server.common.service.ValidationException;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class PromiseServiceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Qualifier("main")
	private PromiseService promiseService;

	@Test
	public void test() throws InterruptedException, ValidationException {
		Long promiseDuration = 500l;
		String token1 = promiseService.getNewPromiseToken(promiseDuration);
		Assert.assertEquals(1, promiseService.getNumberOfOutstandingPromises());
		String token2 = promiseService.getNewPromiseToken(promiseDuration);
		Assert.assertEquals(2, promiseService.getNumberOfOutstandingPromises());
		Thread.sleep(promiseDuration);

		try {
			promiseService.redeem(token1);
			Assert.fail("Expect excection");
		} catch (ValidationException e) {
			// expected
		}

		try {
			promiseService.extendPromise(token1, promiseDuration);
			Assert.fail("Expect excection");
		} catch (ValidationException e) {
			// expected
		}

		Assert.assertEquals(0, promiseService.getNumberOfOutstandingPromises());
		String token3 = promiseService.getNewPromiseToken(promiseDuration);
		Thread.sleep(promiseDuration / 3);
		promiseService.extendPromise(token3, promiseDuration);
		Thread.sleep(promiseDuration / 3);
		promiseService.extendPromise(token3, promiseDuration);
		Thread.sleep(promiseDuration / 3);
		promiseService.extendPromise(token3, promiseDuration);
		Thread.sleep(promiseDuration / 3);
		promiseService.extendPromise(token3, promiseDuration);
		Thread.sleep(promiseDuration / 3);
		promiseService.redeem(token3);

		try {
			promiseService.redeem(token3);
			Assert.fail("Expect excection");
		} catch (ValidationException e) {
			// expected
		}

	}

}
