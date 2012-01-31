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
package com.tasktop.c2c.server.common.service.tests.ajp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

import com.tasktop.c2c.server.web.proxy.ajp.Packet;


public class PacketTest {

	private Packet packet = new Packet();

	@Test
	public void testReadWriteByte() throws IOException {
		for (int x = Byte.MIN_VALUE; x < Byte.MAX_VALUE; x = x + 10) {
			byte b = (byte) x;
			packet.write(b);
			assertEquals(b, packet.readByte());
		}
	}

	@Test
	public void testReadWriteInt() throws IOException {
		for (long x = 0; x < 0xffff; x = x + (0xffff / 1000)) {
			int i = (int) x;
			packet.write(i);
			assertEquals(i, packet.readInt());
		}
	}

	@Test
	public void testReadWriteBoolean() throws IOException {
		packet.write(false);
		assertEquals(false, packet.readBoolean());
		packet.write(true);
		assertEquals(true, packet.readBoolean());
	}

	@Test
	public void testReadWriteStirng() throws IOException {
		String s = UUID.randomUUID().toString();
		packet.write(s);
		String s2 = packet.readString();
		assertEquals(s, s2);
	}
}
