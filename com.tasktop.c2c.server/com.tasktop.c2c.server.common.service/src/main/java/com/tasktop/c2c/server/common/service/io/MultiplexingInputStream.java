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
package com.tasktop.c2c.server.common.service.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.tasktop.c2c.server.common.service.io.MultiplexingOutputStream;
import com.tasktop.c2c.server.common.service.io.PacketType;


/**
 * @see MultiplexingOutputStream
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class MultiplexingInputStream {
	private InputStream input;

	private PacketType packetType;
	private int packetLength;
	private int packetBytesRead;
	private byte[] packetLengthBuffer = new byte[4];

	public MultiplexingInputStream(InputStream input) {
		this.input = input;
	}

	public PacketType getPacketType() throws IOException {
		if (packetType == null || packetBytesRead == packetLength) {
			readPacketType();
		}
		return packetType;
	}

	public int getPacketLength() throws IOException {
		return packetLength;
	}

	private void readPacketType() throws IOException {
		int type = input.read();
		packetType = type == -1 ? null : PacketType.values()[type];
		if (packetType == null) {
			packetLength = 0;
			packetBytesRead = 0;
			return;
		}

		int bytesRead = 0;
		do {
			int read = input.read(packetLengthBuffer, bytesRead, packetLengthBuffer.length - bytesRead);
			if (read == -1) {
				throw new IOException("Expected length");
			}
			bytesRead += read;
		} while (bytesRead < packetLengthBuffer.length);
		packetLength = ByteBuffer.wrap(packetLengthBuffer).getInt();
		packetBytesRead = 0;
	}

	/**
	 * read packet bytes into the given buffer
	 * 
	 * @return the number of bytes read, or -1 if there are no more bytes available in the current packet
	 */
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (packetType == null) {
			return -1;
		}
		int packetBytesRemaining = packetLength - packetBytesRead;
		if (packetBytesRemaining > 0) {
			int read = input.read(buffer, offset, Math.min(length, packetBytesRemaining));
			if (read > 0) {
				packetBytesRead += read;
			}
			return read;
		}
		return -1;
	}

	public int readExitCode() throws IOException {
		if (packetType != PacketType.EXIT_CODE) {
			throw new IllegalStateException();
		}
		int bytesRead = 0;
		byte[] buf = new byte[4];
		while (bytesRead < buf.length) {
			int read = read(buf, bytesRead, buf.length - bytesRead);
			if (read < 1) {
				throw new IOException("Unexpected EOF");
			}
			bytesRead += read;
		}
		final int exitCode = ByteBuffer.wrap(buf).getInt();
		return exitCode;
	}
}
