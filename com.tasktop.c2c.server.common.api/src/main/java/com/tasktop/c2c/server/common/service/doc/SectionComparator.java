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
package com.tasktop.c2c.server.common.service.doc;

import java.util.Comparator;

public class SectionComparator implements Comparator<Section> {

	public int compare(Section o1, Section o2) {
		if (o1 == o2) {
			return 0;
		}
		int i = new Integer(o1.order()).compareTo(o2.order());
		if (i == 0) {
			i = o1.value().compareToIgnoreCase(o2.value());
			if (i == 0) {
				i = o1.value().compareTo(o2.value());
			}
		}
		return i;
	}

}
