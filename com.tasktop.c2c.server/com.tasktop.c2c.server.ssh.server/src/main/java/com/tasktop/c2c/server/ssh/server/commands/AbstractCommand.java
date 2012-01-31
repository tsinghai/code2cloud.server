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
package com.tasktop.c2c.server.ssh.server.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;

/**
 * Implements {@link Cloneable} for the prototype pattern
 */
public abstract class AbstractCommand implements Command, SessionAware, Cloneable {

	protected ServerSession session;

	protected InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	protected ExitCallback callback;

	protected List<String> args;

	public AbstractCommand() {
	}

	public abstract String getName();

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	@Override
	protected AbstractCommand clone() {
		try {
			return (AbstractCommand) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void destroy() {
	}

}
