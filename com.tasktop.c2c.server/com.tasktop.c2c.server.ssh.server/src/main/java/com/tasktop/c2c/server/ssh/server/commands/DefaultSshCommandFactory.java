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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
@Component("sshCommandFactory")
public class DefaultSshCommandFactory extends AbstractSshCommandFactory implements InitializingBean {

	@Autowired(required = true)
	private List<AbstractCommand> commands;

	@Override
	public void afterPropertiesSet() throws Exception {
		Logger log = LoggerFactory.getLogger(getClass().getName());
		for (AbstractCommand command : commands) {
			log.info("enabling ssh command: " + command.getName());
			registerCommand(command);
		}
	}
}
