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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pipe that funnels data from an input stream to an output stream.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public final class InputPipe implements Callable<Void> {

	private final InputStream input;
	private final OutputStream output;
	private final byte[] buffer;
	private IOException exception;
	private boolean eof;
	private Thread interruptThread;
	private Thread callableThread;
	private boolean flush;
	private boolean cancelled;
	private boolean reading;
	private String description = "generic pipe";
	private Logger log = LoggerFactory.getLogger(InputPipe.class.getName());

	/**
	 * 
	 * @param input
	 *            the input stream, from which data is sourced
	 * @param output
	 *            the output stream to which data should be piped
	 * @param bufferSize
	 *            the size of the buffer to use
	 * @param interruptThread
	 *            a thread that should be interrupted when the pipe is closed, or null
	 */
	public InputPipe(InputStream input, OutputStream output, int bufferSize, Thread interruptThread) {
		this.input = input;
		this.output = output;
		this.interruptThread = interruptThread;
		buffer = new byte[bufferSize];
	}

	public IOException getException() {
		return exception;
	}

	public boolean isEof() {
		return eof;
	}

	public InputPipe description(String desc) {
		this.description = desc;
		return this;
	}

	public InputPipe flush(boolean flush) {
		this.flush = flush;
		return this;
	}

	public boolean isFlush() {
		return flush;
	}

	public void setFlush(boolean flush) {
		this.flush = flush;
	}

	@Override
	public Void call() throws Exception {
		synchronized (this) {
			callableThread = Thread.currentThread();
		}

		try {
			log.info("Starting pipe: " + description);
			for (;;) {
				synchronized (this) {
					if (cancelled) {
						break;
					}
					reading = true;
				}
				int bytesRead;
				try {
					bytesRead = input.read(buffer, 0, buffer.length);
				} catch (InterruptedIOException ioe) {
					bytesRead = ioe.bytesTransferred;
					if (bytesRead <= 0) {
						throw ioe;
					}
					synchronized (this) {
						reading = false;
						Thread.interrupted();
						exception = ioe;
						cancelled = true;
					}
				} finally {
					synchronized (this) {
						reading = false;
					}
				}
				if (bytesRead == -1) {
					log.info(description + ": EOF");
					eof = true;
					break;
				} else if (bytesRead > 0) {
					log.info(description + ": bytes read: " + bytesRead);
					output.write(buffer, 0, bytesRead);
					if (flush) {
						output.flush();
					}
				}
			}
		} catch (IOException e) {
			exception = e;
		} finally {
			synchronized (this) {
				Thread.interrupted();
				callableThread = null;
				if (!cancelled && exception != null) {
					log.error("Broken pipe: " + exception.getMessage(), exception);
				}
				if (interruptThread != null && !cancelled) {
					interruptThread.interrupt();
				}
			}
			log.info(description + ": pipe closed");
		}
		return null;
	}

	/**
	 * Gracefully cancel the pipe. The pipe will complete writing any data that was read, and then exit
	 */
	public void cancel() {
		synchronized (this) {
			interruptThread = null;
			cancelled = true;
			if (callableThread != null) {
				log.info(description + ": cancel initiated");
				if (reading) {
					callableThread.interrupt();
				}
			}
		}
	}
}
