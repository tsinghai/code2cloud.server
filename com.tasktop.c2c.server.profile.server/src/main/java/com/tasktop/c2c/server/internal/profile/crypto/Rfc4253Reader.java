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
package com.tasktop.c2c.server.internal.profile.crypto;

import java.io.IOException;
import java.math.BigInteger;

/**
 * a reader for reading data from various public key file formats
 * 
 * @author David Green (Tasktop Technologies Inc.)
 * 
 * @see http://tools.ietf.org/html/rfc4253
 */
public class Rfc4253Reader {
	private byte[] buffer;
	private int offset;

	public Rfc4253Reader(byte[] buffer, int offset) {
		this.buffer = buffer;
		this.offset = offset;
	}

	/**
	 * read an mpint per http://tools.ietf.org/html/rfc4253#section-6.6
	 */
	public BigInteger readMpint() throws IOException {
		byte[] bytes = readBytes();
		return bytes.length == 0 ? BigInteger.valueOf(0) : new BigInteger(bytes);
	}

	public byte[] readBytes() throws IOException {
		int size = readUint32();
		if (size < 0) {
			underflow();
		}
		if (offset + size > buffer.length) {
			underflow();
		}
		int start = offset;
		offset += size;
		byte[] bytes = new byte[size];
		System.arraycopy(buffer, start, bytes, 0, size);
		return bytes;
	}

	private byte readByte() {
		if (offset < buffer.length) {
			return buffer[offset++];
		}
		return -1;
	}

	public static int toUnsignedByte(byte b) {
		return b & 0xFF;
	}

	public int readUint32() throws IOException {
		int b1 = toUnsignedByte(readByte());
		int b2 = toUnsignedByte(readByte());
		int b3 = toUnsignedByte(readByte());
		int b4 = toUnsignedByte(readByte());
		if (b4 == -1) {
			underflow();
		}
		return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
	}

	public String readString() throws IOException {
		int size = readUint32();
		if (size < 0) {
			underflow();
		}
		if (offset + size > buffer.length) {
			underflow();
		}
		int start = offset;
		offset += size;
		return new String(buffer, start, size);
	}

	private void underflow() throws IOException {
		throw new IOException();
	}
}
