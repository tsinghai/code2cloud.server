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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class AbstractSshCommandFactory implements CommandFactory {

	private static final Pattern PATTERN_COMMAND = Pattern.compile("('.*?')|(\".*?\")|((\\S|(\\\\.))+)");

	private Map<String, AbstractCommand> commandsByName = new HashMap<String, AbstractCommand>();

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public Command createCommand(String commandString) {
		List<String> arguments = parseCommand(commandString);
		if (!arguments.isEmpty()) {
			String commandName = arguments.get(0);
			AbstractCommand command = commandsByName.get(commandName);
			if (command != null) {
				// command is prototype, so clone it
				command = command.clone();
				applicationContext.getAutowireCapableBeanFactory().autowireBean(command);
				arguments.remove(0);
				command.setArgs(arguments);
				return command;
			}
		}
		return new UnrecognizedCommandHandler(commandString);
	}

	private List<String> parseCommand(String command) {
		List<String> args = new ArrayList<String>(3);

		Matcher matcher = PATTERN_COMMAND.matcher(command);
		while (matcher.find()) {
			String a = matcher.group(1);
			if (a == null) {
				a = matcher.group(2);
			}
			if (a == null) {
				a = matcher.group(3);
			} else {
				a = a.substring(1, a.length() - 1).replaceAll("\\\\(.)", "$1");
			}
			args.add(a);
		}
		return args;
	}

	public void registerCommand(AbstractCommand command) {
		commandsByName.put(command.getName(), command);
	}

	public void setCommandsByName(Map<String, AbstractCommand> commandsByName) {
		this.commandsByName = commandsByName;
	}

}
