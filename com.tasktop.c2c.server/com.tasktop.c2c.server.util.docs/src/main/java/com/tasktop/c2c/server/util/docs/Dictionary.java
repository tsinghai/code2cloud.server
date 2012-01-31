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
package com.tasktop.c2c.server.util.docs;

import java.util.Random;

class Dictionary {
	private Random random;

	private String[] words;

	public Dictionary() {
		this(1L);
	}

	public Dictionary(long randomSeed) {
		random = new Random(randomSeed);
		load();
	}

	private void load() {
		String text = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		words = text.split("\\s");
	}

	public String createText(int wordCount) {
		int sentenceLength = 10;
		String text = "";
		while (wordCount > 0) {
			--wordCount;
			String word = randomWord();

			if (text.length() != 0) {
				if (wordCount % sentenceLength == 0) {
					text += ". ";
				}
				text += " ";
			}
			text += word;
		}
		return text;
	}

	public String randomWord() {
		return words[Math.abs(random.nextInt()) % words.length];
	}

}
