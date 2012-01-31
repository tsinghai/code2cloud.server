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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * a reader for reading data from various public key file formats
 * 
 * @author David Green (Tasktop Technologies Inc.)
 * 
 * @see http://tools.ietf.org/html/rfc4253
 */
public class Rfc4253Writer {
	private ByteArrayOutputStream output = new ByteArrayOutputStream();

	public Rfc4253Writer() {

	}

	/**
	 * write an mpint per http://tools.ietf.org/html/rfc4253#section-6.6
	 */
	public void writeMpint(BigInteger i) throws IOException {
		writeBytes(i.toByteArray());
	}

	public void writeBytes(byte[] bytes) throws IOException {
		writeUint32(bytes.length);
		output.write(bytes);
	}

	public byte[] getBytes() {
		return output.toByteArray();
	}

	public static byte toSignedByte(int i) {
		if (i > 255) {
			throw new IllegalStateException();
		}
		return (byte) (i | 0x00);
	}

	private void writeUint32(int i) throws IOException {
		byte b1 = toSignedByte(i >> 24);
		byte b2 = toSignedByte(i >> 16 & 0x00FFFFFF);
		byte b3 = toSignedByte(i >> 8 & 0x0000FFFF);
		byte b4 = toSignedByte(i & 0x000000FF);
		output.write(b1);
		output.write(b2);
		output.write(b3);
		output.write(b4);
	}
}
