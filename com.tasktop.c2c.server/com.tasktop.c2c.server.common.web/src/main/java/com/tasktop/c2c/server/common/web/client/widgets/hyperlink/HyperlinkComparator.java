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
package com.tasktop.c2c.server.common.web.client.widgets.hyperlink;

import java.util.Comparator;

/**
 * Orders hyperlinks by offset, larger hyperlinks first.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class HyperlinkComparator implements Comparator<Hyperlink> {

	@Override
	public int compare(Hyperlink arg0, Hyperlink arg1) {
		if (arg0 == arg1) {
			return 0;
		}
		int i = arg0.getOffset() - arg1.getOffset();
		if (i == 0) {
			i = arg1.getLength() - arg0.getLength();
		}
		return i;
	}

}
