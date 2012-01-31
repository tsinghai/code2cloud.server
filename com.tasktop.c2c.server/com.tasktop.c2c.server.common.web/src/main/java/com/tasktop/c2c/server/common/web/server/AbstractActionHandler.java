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
package com.tasktop.c2c.server.common.web.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.customware.gwt.dispatch.server.SimpleActionHandler;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.DispatchException;
import net.customware.gwt.dispatch.shared.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;

/**
 * FIXME much of this is duplicated form AbstractAutowiredRemoteService
 * 
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractActionHandler<A extends Action<R>, R extends Result> extends SimpleActionHandler<A, R> {

	@Autowired
	private MessageSource messageSource;

	private Pattern validationPattern = Pattern.compile("\\[(\\p{Alnum}+)|(\\-?\\p{Digit}+)\\]");

	protected void handle(ValidationException exception) throws DispatchException {
		List<String> messages = new ArrayList<String>();
		if (exception.getErrors() != null) {
			for (ObjectError error : exception.getErrors().getAllErrors()) {
				try {
					// Check to see if this is one of our custom messages, intended for a multi-object form - if it is,
					// chop it up.
					Matcher matcher = validationPattern.matcher(error.getCode());
					String origErrorMsg = messageSource.getMessage(error, getLocale());

					if (matcher.find()) {
						// This is one of our custom messages - try message lookup with the custom section removed
						String newCode = error.getCode().substring(0, matcher.start());

						// This will return a new error message if it's present, or the original if it wasn't.
						String libraryErrorMessage = messageSource.getMessage(newCode, error.getArguments(),
								origErrorMsg, getLocale());

						// Grab our first match, and then issue another find() call to grab the second group.
						String className = matcher.group(1);

						String classId = null;
						if (matcher.find()) {
							classId = matcher.group(2);
						}

						// Add in this augmented message to our message list, so that it can be detected and handled by
						// our controls.
						messages.add(String.format("%s|%s|%s|%s", libraryErrorMessage, newCode, className, classId));
					} else {
						// It's not, so just do the normal processing.
						messages.add(origErrorMsg);
					}

				} catch (NoSuchMessageException t) {

					messages.add(t.getMessage());
					// FIXME: development only
					// throw t;
				}
			}
		} else {
			// This is the case for exceptions coming over the rest client.
			String[] messageSplits = exception.getMessage().split(",");
			messages.addAll(Arrays.asList(messageSplits));
		}
		throw new ActionException(new ValidationFailedException(messages));
	}

	// TODO : handle this correctly
	protected void handle(ConcurrentUpdateException exception) throws DispatchException {
		throw new ActionException(new ValidationFailedException(
				Arrays.asList("The object has been modified since it was loaded")));
	}

	protected void handle(EntityNotFoundException e) throws DispatchException {
		throw new ActionException(new NoSuchEntityException());
	}

	private Locale getLocale() {
		// FIXME get user's locale
		return Locale.ENGLISH;
	}
}
