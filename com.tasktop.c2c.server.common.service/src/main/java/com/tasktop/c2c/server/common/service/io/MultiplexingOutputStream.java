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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A means of multiplexing output over a single stream
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class MultiplexingOutputStream implements Closeable {
	private final class PacketStream extends OutputStream {

		private final PacketType type;
		private final byte[] lengthBuffer = new byte[4];

		public PacketStream(PacketType type) {
			this.type = type;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b });
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			// LoggerFactory.getLogger(MultiplexingOutputStream.class.getName()).log(Level.INFO,
			// "Writing " + len + " bytes to " + type);
			ByteBuffer.wrap(lengthBuffer).putInt(len);
			synchronized (output) {
				// we write the type ordinal (1 byte) followed by length (a 32-bit integer, 4 bytes)
				// followed by data
				output.write(type.ordinal());
				output.write(lengthBuffer);
				output.write(b, off, len);
			}
		}

		@Override
		public void flush() throws IOException {
			synchronized (output) {
				output.flush();
			}
		}
	}

	private OutputStream output;

	public MultiplexingOutputStream(OutputStream output) {
		this.output = output;
	}

	public OutputStream stream(PacketType type) {
		return new PacketStream(type);
	}

	public void writeExitCode(int exitValue) throws IOException {
		OutputStream outputStream = stream(PacketType.EXIT_CODE);
		byte[] lengthBuffer = new byte[4];
		ByteBuffer.wrap(lengthBuffer).putInt(exitValue);
		outputStream.write(lengthBuffer);
		outputStream.flush();
	}

	public OutputStream getDelegate() {
		return output;
	}

	public void close() throws IOException {
		output.close();
	}
}
