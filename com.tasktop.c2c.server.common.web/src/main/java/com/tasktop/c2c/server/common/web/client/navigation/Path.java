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
package com.tasktop.c2c.server.common.web.client.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tasktop.c2c.server.common.web.shared.URLEncoding;

public class Path {
	/**
	 * The character used to delimit the hashtag portion of the URL path. Note that this is different from the
	 * traditional '#' character due to limitations with IE, see bug 1833.
	 */
	// CAREFUL: changing this has wide-ranging implications, notably:
	// * valid PathMapping names
	// * wiki page name validation
	// * wiki page rendering
	// * must be a valid URL path character, not reserved - see http://www.ietf.org/rfc/rfc2396.txt
	public static final char HASHTAG_DELIMITER = '-';

	public static final String SUBPAGE_DELIMITER = "/wiki/p/";

	/**
	 * the argument key for the hashtag portion of the uri
	 * 
	 * @see #configureArgs(String)
	 */
	public static final String HASHTAG = "#";

	public static final String PROJECT_BASE = "projects";
	public static final String PROJECT_ID = "projectIdentifier";

	private final String path;
	private final String hashtag;

	private final PathElement[] pathElements;
	private final int argumentCount;
	private boolean hasWildcard;

	public Path(String stringPath) {
		// Keep a copy of this string around.
		path = stringPath;
		hashtag = computeHashtag(stringPath);

		// Now, break out and calculate the elements of our path.
		int argumentCount = 0;
		// the implementation may seem a little complicated but
		// we can't use regular expressions here.
		List<PathElement> pathElements = new ArrayList<PathElement>();
		String[] parts = path.split("/");
		for (String part : parts) {
			if (hasWildcard) {
				throw new IllegalArgumentException();
			}
			if (part.startsWith("{") && part.endsWith("}")) {
				ArgumentPathElement pathElement = new ArgumentPathElement(argumentCount, part.substring(1,
						part.length() - 1));
				hasWildcard = pathElement.isWildcard();
				pathElements.add(pathElement);
				++argumentCount;
			} else {
				pathElements.add(new LiteralPathElement(part));
			}
		}

		this.argumentCount = argumentCount;
		this.pathElements = pathElements.toArray(new PathElement[pathElements.size()]);
	}

	private String computeHashtag(String stringPath) {

		if (!stringPath.contains(SUBPAGE_DELIMITER)) {
			return null;
		}

		String subPath = stringPath.substring(stringPath.indexOf(SUBPAGE_DELIMITER) + SUBPAGE_DELIMITER.length());
		int index = subPath.indexOf(HASHTAG_DELIMITER);
		if (index >= 0) {
			return subPath.substring(index + 1);
		}
		return null;
	}

	/**
	 * get the raw path
	 * 
	 * @see #getHashTag()
	 * @see #getRawPagePath()
	 */
	public String getRawPath() {
		return path;
	}

	/**
	 * get the hashtag portion of the {@link #getRawPath() raw path} (not including the leading '#')
	 */
	public String getHashTag() {
		return hashtag;
	}

	/**
	 * get the {@link #getRawPath() raw path} without the {@link #getHashTag() hashtag}.
	 */
	public String getRawPagePath() {
		if (hashtag == null) {
			return path;
		}
		return path.substring(0, path.length() - (hashtag.length() + 1));
	}

	/**
	 * indicate if this path is the same as the other path. Sameness is determined by the equality of
	 * {@link #getRawPagePath()}.
	 */
	public boolean sameAs(Path otherPath) {
		if (otherPath == null) {
			return false;
		}
		if (otherPath == this || otherPath.getRawPagePath().equals(getRawPagePath())) {
			return true;
		}
		return false;
	}

	public boolean matches(String path) {
		return matches(path.split("/"));
	}

	public boolean matches(String[] path) {
		if (path.length == pathElements.length || (hasWildcard && path.length > pathElements.length)) {
			for (int x = 0; x < path.length; ++x) {
				PathElement pathElement = pathElements[x];
				if (!pathElement.matches(path[x])) {
					return false;
				}
				if (pathElement.isWildcard()) {
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @see #HASHTAG
	 */
	public Args configureArgs(String path) {
		return configureArgs(path.split("/"));
	}

	/**
	 * @see #HASHTAG
	 */
	public Args configureArgs(String[] path) {
		Args retArgs = new Args();

		String hashtag = null;
		for (int x = 0; x < path.length; ++x) {
			PathElement pathElement = pathElements[x];
			if (pathElement.isWildcard()) {
				String arg = path[x];
				for (int y = x + 1; y < path.length; ++y) {
					arg += '/';
					arg += path[y];
				}
				if (this.path.contains(SUBPAGE_DELIMITER)) {
					int idxOfHash = arg.indexOf(HASHTAG_DELIMITER);
					if (idxOfHash != -1) {
						hashtag = arg.substring(idxOfHash + 1);
						arg = arg.substring(0, idxOfHash);
					}
				}
				pathElement.configure(retArgs, arg);
				break;
			} else {
				pathElement.configure(retArgs, path[x]);
			}
		}
		if (hashtag != null) {
			retArgs.map(Path.HASHTAG, hashtag);
		}

		return retArgs;
	}

	public String uri(Object... args) {
		if (args.length != this.argumentCount) {
			throw new IllegalArgumentException(args.length + " != " + this.argumentCount);
		}

		String path = "";
		for (PathElement element : pathElements) {
			if (path.length() > 0) {
				path += "/";
			}
			path += element.pathValue(args);
		}
		return path;
	}

	public String uri(Map<String, String> args) {
		if (args.size() != this.argumentCount) {
			throw new IllegalArgumentException(args.size() + " != " + this.argumentCount);
		}

		String path = "";
		for (PathElement element : pathElements) {
			if (path.length() > 0) {
				path += "/";
			}
			if (element instanceof ArgumentPathElement) {
				String argName = ((ArgumentPathElement) element).argumentName;
				String argVal = args.get(argName);
				path += (argVal == null ? "" : URLEncoding.encode(argVal, new char[] { '/' }));
			} else {
				// If it's not an argument, it ignores the args anyways.
				path += element.pathValue(null);
			}
		}
		return path;
	}

	public boolean containsNamedArgument(String argName) {
		for (PathElement element : pathElements) {
			if (element instanceof ArgumentPathElement) {
				String curArgName = ((ArgumentPathElement) element).argumentName;
				if (curArgName.equals(argName)) {
					return true;
				}
			}
		}

		return false;
	}

	public int getArgumentCount() {
		return argumentCount;
	}

	public boolean isArgumentInteger(int n) {
		int i = 0;
		for (PathElement element : pathElements) {
			if (element instanceof ArgumentPathElement) {
				if (i == n) {
					return ((ArgumentPathElement) element).integerType;
				}
				i++;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return path;
	}

	private interface PathElement {
		boolean matches(String s);

		String pathValue(Object[] args);

		void configure(Args args, String part);

		boolean isWildcard();
	}

	private static class LiteralPathElement implements PathElement {
		String literal;

		public LiteralPathElement(String literal) {
			this.literal = literal;
		}

		@Override
		public boolean matches(String part) {
			return literal.equals(part);
		}

		@Override
		public void configure(Args args, String part) {
			// nothing to do
		}

		@Override
		public String pathValue(Object[] args) {
			return literal;
		}

		@Override
		public boolean isWildcard() {
			return false;
		}
	}

	private static class ArgumentPathElement implements PathElement {
		private final String argumentName;
		private final int argumentIndex;
		private boolean integerType = false;
		private boolean wildcard;

		public ArgumentPathElement(int argumentIndex, String argumentName) {
			this.argumentIndex = argumentIndex;
			String[] argumentNameParts = argumentName.split(":");
			if (argumentNameParts.length == 2) {
				String typeName = argumentNameParts[1];
				if (typeName.equals("Integer") || typeName.equals("Long")) {
					integerType = true;
				} else if (typeName.equals("*")) {
					wildcard = true;
				} else {
					throw new IllegalArgumentException(typeName);
				}
			} else if (argumentNameParts.length != 1) {
				throw new IllegalArgumentException(argumentName);
			}
			this.argumentName = argumentNameParts[0];

		}

		@Override
		public boolean matches(String part) {
			if (part != null && part.length() > 0) {
				if (integerType) {
					try {
						Long.parseLong(part);
					} catch (NumberFormatException e) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public void configure(Args args, String part) {
			args.map(argumentName, part == null ? "" : URLEncoding.decode(part));
		}

		@Override
		public String pathValue(Object[] args) {
			Object arg = args[argumentIndex];
			return arg == null ? "" : URLEncoding.encode(arg.toString(), new char[] { '/' });
		}

		public boolean isWildcard() {
			return wildcard;
		}

	}
}
