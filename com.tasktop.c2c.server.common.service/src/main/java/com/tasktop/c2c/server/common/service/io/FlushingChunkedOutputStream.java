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
import java.io.OutputStream;

import org.apache.commons.httpclient.ChunkedOutputStream;

/**
 * A chunked output stream that flushes the underlying chunk cache when flushed.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class FlushingChunkedOutputStream extends ChunkedOutputStream {

	public FlushingChunkedOutputStream(OutputStream stream, int bufferSize) throws IOException {
		super(stream, bufferSize);
	}

	public FlushingChunkedOutputStream(OutputStream stream) throws IOException {
		super(stream);
	}

	@Override
	public void flush() throws IOException {
		flushCache();
		super.flush();
	}
}
