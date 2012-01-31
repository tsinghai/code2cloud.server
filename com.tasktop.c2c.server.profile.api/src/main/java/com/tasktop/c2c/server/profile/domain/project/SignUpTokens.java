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
package com.tasktop.c2c.server.profile.domain.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SignUpTokens implements Serializable {

	private List<SignUpToken> tokens = new ArrayList<SignUpToken>();

	public List<SignUpToken> getTokens() {
		return tokens;
	}

	public void setTokens(List<SignUpToken> tokens) {
		this.tokens = tokens;
	}

	public void add(SignUpToken signUpToken) {
		tokens.add(signUpToken);
	}

}
