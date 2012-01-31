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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;


@Component
@Qualifier("main")
public class PublicKeyReaderManager implements PublicKeyReader {

	private List<PublicKeyReader> publicKeyReaders = new ArrayList<PublicKeyReader>();
	{
		publicKeyReaders.add(new OpenSSHPublicKeyReader());
		publicKeyReaders.add(new Rfc4716PublicKeyReader());
		publicKeyReaders.add(new PemsPublicKeyReader());
	}

	@Override
	public SshPublicKey readPublicKey(String keySpec) {
		SshPublicKey key = null;
		for (PublicKeyReader reader : publicKeyReaders) {
			key = reader.readPublicKey(keySpec);
			if (key != null) {
				break;
			}
		}
		return key;
	}

	public List<PublicKeyReader> getPublicKeyReaders() {
		return publicKeyReaders;
	}

	public void setPublicKeyReaders(List<PublicKeyReader> publicKeyReaders) {
		this.publicKeyReaders = publicKeyReaders;
	}

}
