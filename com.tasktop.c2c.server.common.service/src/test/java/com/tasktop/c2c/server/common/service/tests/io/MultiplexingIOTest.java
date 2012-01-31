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
package com.tasktop.c2c.server.common.service.tests.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import com.tasktop.c2c.server.common.service.io.MultiplexingInputStream;
import com.tasktop.c2c.server.common.service.io.MultiplexingOutputStream;
import com.tasktop.c2c.server.common.service.io.PacketType;


/**
 * @author David Green (Tasktop Technologies Inc.)
 * 
 */
public class MultiplexingIOTest {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Test
	public void testIO() throws IOException {
		MultiplexingOutputStream mox = new MultiplexingOutputStream(out);
		OutputStream stdout = mox.stream(PacketType.STDOUT);
		OutputStream stderr = mox.stream(PacketType.STDERR);

		byte[] bytes1 = "abc".getBytes();
		stdout.write(bytes1);
		byte[] bytes2 = "abcd".getBytes();
		stdout.write(bytes2);
		byte[] bytes3 = "abcde".getBytes();
		stderr.write(bytes3);
		mox.writeExitCode(235);

		MultiplexingInputStream mix = new MultiplexingInputStream(new ByteArrayInputStream(out.toByteArray()));

		assertPacket(PacketType.STDOUT, bytes1, mix);
		assertPacket(PacketType.STDOUT, bytes2, mix);
		assertPacket(PacketType.STDERR, bytes3, mix);
		assertEquals(PacketType.EXIT_CODE, mix.getPacketType());
		assertEquals(235, mix.readExitCode());
	}

	private void assertPacket(PacketType expectedPacketType, byte[] expectedBytes, MultiplexingInputStream mix)
			throws IOException {
		assertEquals(expectedPacketType, mix.getPacketType());
		assertEquals(expectedBytes.length, mix.getPacketLength());
		byte[] b = new byte[mix.getPacketLength()];
		int bytesRead = mix.read(b, 0, mix.getPacketLength());
		assertEquals(mix.getPacketLength(), bytesRead);
		assertArrayEquals(expectedBytes, b);
	}

}
