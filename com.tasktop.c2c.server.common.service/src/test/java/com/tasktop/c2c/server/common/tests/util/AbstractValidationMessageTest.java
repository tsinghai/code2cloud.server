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
package com.tasktop.c2c.server.common.tests.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Validator;

/**
 * Base class to ensure that all the message keys that a validator references are defined in message bundles. To
 * Implement a concrete test, set the javaFilenames and the bundleFilenames.
 */
public abstract class AbstractValidationMessageTest {
	private Collection<String> validatorJavaFilenames;
	private Collection<String> messageBundleFilenames;

	@Test
	public void ensureMessagesDefinedForValidator() {
		Logger logger = LoggerFactory.getLogger(getClass().getName());

		Set<String> messagePropertiesNeeded = new TreeSet<String>();

		Assert.assertFalse("No validator file names specified", validatorJavaFilenames.isEmpty());
		Assert.assertFalse("No bundle file names specified", messageBundleFilenames.isEmpty());

		for (String validatorJavaFilename : validatorJavaFilenames) {
			logger.debug("Checking " + validatorJavaFilename);

			String validatorText = loadFile(validatorJavaFilename);

			final int initialSize = messagePropertiesNeeded.size();

			Matcher matcher = rejectValuePattern.matcher(validatorText);

			while (matcher.find()) {
				String rejectValue = validatorText.substring(matcher.start(), matcher.end());
				if (!rejectValue.contains("(\"")) {
					// This happens when we pass in a parameter or use a constant. Will miss these messages for now.
					continue;
				}
				String property = matcher.group(1).replace("\"", "");
				String errorCode = matcher.group(2).replace("\"", "");
				messagePropertiesNeeded.add(errorCode + "." + property);
			}

			matcher = rejectIfEmptyPattern.matcher(validatorText);

			while (matcher.find()) {
				String rejectValue = validatorText.substring(matcher.start(), matcher.end());
				int firstComma = rejectValue.indexOf(",");
				int secondComma = rejectValue.indexOf(",", firstComma + 1);
				String property = rejectValue.substring(firstComma + 3, secondComma - 1);
				String errorCode = rejectValue.substring(secondComma + 3, rejectValue.indexOf("\")"));
				messagePropertiesNeeded.add(errorCode + "." + property);
			}

			matcher = rejectPattern.matcher(validatorText);

			while (matcher.find()) {
				String rejectValue = validatorText.substring(matcher.start(), matcher.end());
				String errorCode = rejectValue.substring(rejectValue.indexOf("(\"") + 2, rejectValue.indexOf("\")"));
				messagePropertiesNeeded.add(errorCode);
			}

			Assert.assertTrue(validatorJavaFilename + " has no validation message keys!",
					initialSize < messagePropertiesNeeded.size());
		}

		final int numKeysFoundInSource = messagePropertiesNeeded.size();
		logger.info("Found " + numKeysFoundInSource + " message keys: " + messagePropertiesNeeded);

		int numKeysFoundInBundles = 0;
		for (String messagePropertyFile : messageBundleFilenames) {
			String bundleText = loadFile(messagePropertyFile);
			for (String line : bundleText.split("\n")) {
				String name = line.split("=")[0].trim();
				++numKeysFoundInBundles;
				Iterator<String> messagePropertiesNeededIterator = messagePropertiesNeeded.iterator();

				while (messagePropertiesNeededIterator.hasNext()) {
					String messagePropertyNeeded = messagePropertiesNeededIterator.next();

					logger.debug("\tlooking for: " + messagePropertyNeeded);

					// Remove exact matches as well as the general messages that spring will use as well.
					if (messagePropertyNeeded.startsWith(name)) {
						messagePropertiesNeededIterator.remove();
					}
				}
			}
		}

		Assert.assertTrue("Properties not defined for: " + messagePropertiesNeeded.toString(),
				messagePropertiesNeeded.isEmpty());

		logger.info(numKeysFoundInSource + " keys found in source, " + numKeysFoundInBundles + " found in bundles");

	}

	private Pattern rejectPattern = Pattern.compile("reject\\([^,]*\\)");

	private Pattern rejectValuePattern = Pattern.compile("rejectValue\\(([^,]*), ([^,\\)]*)");
	// FIXME I think these patterns should quote the parens
	private Pattern rejectIfEmptyPattern = Pattern.compile("rejectIfEmpty[OrWhitespace]*([^,]*,[^,]*,.*)");

	private String loadFile(String fileName) {
		StringWriter out = new StringWriter();
		try {
			FileInputStream fileStream = new FileInputStream(fileName);
			Reader in = new InputStreamReader(fileStream);
			try {
				int i;
				while ((i = in.read()) != -1) {
					out.write(i);
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Can't read: " + fileName, e);
		}
		return out.toString();
	}

	public Collection<String> getValidatorJavaFilenames() {
		return validatorJavaFilenames;
	}

	public void setValidatorJavaFilenames(Collection<String> validatorJavaFilenames) {
		this.validatorJavaFilenames = validatorJavaFilenames;
	}

	public Collection<String> getMessageBundleFilenames() {
		return messageBundleFilenames;
	}

	public void setMessageBundleFilenames(Collection<String> messageBundleFilenames) {
		this.messageBundleFilenames = messageBundleFilenames;
	}

	public void computeValidatorJavaFilenames(String javaSourcePathPrefix, Collection<Validator> validators) {
		if (validatorJavaFilenames == null) {
			validatorJavaFilenames = new TreeSet<String>();
		}
		for (Validator validator : validators) {
			String name = javaSourcePathPrefix + '/' + validator.getClass().getName().replace('.', '/') + ".java";
			validatorJavaFilenames.add(name);
		}
	}
}
