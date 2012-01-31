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

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class UnrecognizedCommandHandler extends AbstractCommand implements Command {

	private String command;

	public UnrecognizedCommandHandler(String command) {
		this.command = command;
	}

	@Override
	public String getName() {
		return "*";
	}

	@Override
	public void start(Environment env) throws IOException {
		PrintWriter errorWriter = new PrintWriter(err);
		errorWriter.println("Unrecognized command: " + command);
		errorWriter.flush();
		callback.onExit(-1);
	}

	@Override
	public void destroy() {
	}

}
