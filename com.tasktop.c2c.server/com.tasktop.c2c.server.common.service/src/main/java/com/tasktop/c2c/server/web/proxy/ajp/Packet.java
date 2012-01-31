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
package com.tasktop.c2c.server.web.proxy.ajp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * an AJP packet
 * 
 * @author David Green
 */
public class Packet {
	/**
	 * maximum packet size
	 */
	public static final int MAX_SIZE = 1024 * 8;

	private static final int MAX_DATA_SIZE = MAX_SIZE - 8;

	private static final int AJP_RESPONSE_MARKER = 0x4142;

	private byte[] packetData = new byte[MAX_SIZE];
	private int offset;

	private int readOffset;
	private Charset charset = Charset.forName("iso-8859-1");

	public Packet() {
		reset();
	}

	/**
	 * reset the packet for reuse
	 */
	public void reset() {
		// offset of 4 so that we can set up the initial bytes and length
		offset = 4;
		readOffset = offset;
	}

	public void write(byte b) throws IOException {
		if (offset >= packetData.length) {
			overflow();
		}
		packetData[offset++] = b;
	}

	private void overflow() throws IOException {
		throw new IOException("Write error: buffer overflow");
	}

	public void write(int i) throws IOException {
		byte b1 = (byte) (0xff & (i >> 8));
		byte b2 = (byte) (0xff & i);
		write(b1);
		write(b2);
	}

	public void write(boolean b) throws IOException {
		write((byte) (b ? 1 : 0));
	}

	public void write(String s) throws IOException {
		if (s != null) {
			final int stringLength = s.length();
			write(stringLength);
			write(s.getBytes(charset));
			write((byte) 0);
		} else {
			write(-1);
		}
	}

	public void write(byte[] bytes) throws IOException {
		for (byte b : bytes) {
			write(b);
		}
	}

	public byte readByte() throws IOException {
		if (readOffset >= offset) {
			underflow();
		}
		return packetData[readOffset++];
	}

	public void unreadByte() throws IOException {
		if (--readOffset < 0) {
			++readOffset;
			underflow();
		}
	}

	private void underflow() throws IOException {
		throw new IOException("Read error: buffer underflow");
	}

	public int readInt() throws IOException {
		byte b1 = readByte();
		byte b2 = readByte();
		return (0xff00 & (b1 << 8)) + (b2 & 0xff);
	}

	public boolean readBoolean() throws IOException {
		return readByte() == 0 ? false : true;
	}

	public String readString() throws IOException {
		int length = readInt();
		if (length == -1) {
			return null;
		}
		if (readOffset + length > offset) {
			underflow();
		}
		String string = length == 0 ? "" : new String(packetData, readOffset, length, charset);
		readOffset += length + 1; // +1 for 0 terminator
		return string;
	}

	/**
	 * copy bytes from the given input stream into the packet buffer
	 * 
	 * @param source
	 *            the data source
	 * @param bytes
	 *            the number of bytes to read, or <code>Integer.MAX_VALUE</code>
	 * @return the number of bytes read from the stream
	 */
	public int stream(InputStream source, int bytes) throws IOException {
		if (source == null) {
			return 0;
		}
		int byteCount = Math.min(MAX_DATA_SIZE, bytes);
		int totalBytesRead = 0;
		int bytesRead;

		int totalOffset = offset;
		write(byteCount);

		do {
			bytesRead = source.read(packetData, offset, byteCount - totalBytesRead);
			if (bytesRead != -1) {
				totalBytesRead += bytesRead;
				offset += bytesRead;
			}

		} while (bytesRead != -1 && totalBytesRead < byteCount);

		if (totalBytesRead != byteCount) {
			// seek back and rewrite the count
			int nowOffset = offset;
			offset = totalOffset;
			write(totalBytesRead);
			offset = nowOffset;
		}

		return totalBytesRead;
	}

	public void write(OutputStream output) throws IOException {
		int packetSize = offset;

		// setup packet prefix
		offset = 0;
		write(0x1234); // standard packet prefix
		write(packetSize - 4); // size of packet (minus packet prefix)
		offset = packetSize;

		// write the output
		output.write(packetData, 0, packetSize);
	}

	public void read(InputStream source) throws IOException {
		offset = 0;
		readOffset = 0;

		do {
			int bytesRead = source.read(packetData, offset, 4 - offset);
			if (bytesRead == -1) {
				throw new IOException();
			}
			offset += bytesRead;
		} while (offset < 4);

		int marker = readInt();
		if (marker != AJP_RESPONSE_MARKER) {
			responseProtocolError();
		}
		int length = readInt();
		while (length > 0) {
			int bytesRead = source.read(packetData, offset, length);
			if (bytesRead == -1) {
				throw new IOException();
			}
			length -= bytesRead;
			offset += bytesRead;
		}
	}

	/**
	 * indicate how many bytes are available for reading before an underflow occurs
	 */
	public int available() {
		return offset - readOffset;
	}

	public void copy(OutputStream output) throws IOException {
		int byteCount = available();
		if (byteCount <= 0) {
			underflow();
		}
		int packetDataSize = readInt();
		// sanity check: we've already read the message type, so we should have 2 bytes for the length
		// and one for the packet terminator.
		if (packetDataSize != (byteCount - 3)) {
			responseProtocolError();
		}
		output.write(packetData, readOffset, packetDataSize);
		readOffset += byteCount;
	}

	private void responseProtocolError() throws IOException {
		throw new IOException("AJP protocol error: unexpected response");
	}

}
