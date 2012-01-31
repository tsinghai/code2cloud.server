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
package com.tasktop.c2c.server.common.service.domain.criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;

public class CriteriaParser {

	public static Criteria parse(String criteria) {
		CriteriaParser parser = new CriteriaParser();
		parser.setText(criteria);
		return parser.parse();
	}

	private String text;
	private int offset;

	public void setText(String text) {
		this.text = text;
	}

	public Criteria parse() {
		// implementation note: this parser is a bit of a mess, but it must work with GWT -> JavaScript

		List<Criteria> stack = new ArrayList<Criteria>();

		String word;
		while ((word = nextWord()) != null) {
			if (word.equals("(")) {
				NaryCriteria criteria = new NaryCriteria();
				criteria.setOperator(Operator.OR);
				stack.add(criteria);
			} else if (word.equals(")")) {
				if (stack.isEmpty()) {
					throw new IllegalStateException("Unexpected token " + word + " near offset " + offset);
				}
				Criteria last = stack.remove(stack.size() - 1);
				if (!(last instanceof NaryCriteria)) {
					throw new IllegalStateException("Unexpected token " + word + " near offset " + offset);
				}
				if (stack.isEmpty()) {
					NaryCriteria criteria = new NaryCriteria();
					criteria.setOperator(Operator.OR);
					criteria.addSubCriteria(last);
					stack.add(criteria);
				} else {
					Criteria criteria = stack.get(stack.size() - 1);
					if (criteria instanceof NaryCriteria) {
						NaryCriteria nary = (NaryCriteria) criteria;
						nary.addSubCriteria(last);
					} else {
						throw new IllegalStateException("Illegal syntax near offset " + offset);
					}
				}
			} else {
				if (word.startsWith("'") && word.endsWith("'")) {
					throw new IllegalStateException("Unexpected literal " + word + " near offset " + offset);
				} else {
					if (Operator.NOT.toQueryString().equals(word)) {
						throw new IllegalStateException("not implemented");
					} else {
						Operator operator = Operator.fromQueryString(word);
						if (operator != null && (operator == Operator.OR || operator == Operator.AND)) {
							Criteria last = null;
							if (!stack.isEmpty()) {
								last = stack.get(stack.size() - 1);
							}
							if (last == null) {
								throw new IllegalStateException("Unexpected operator " + word + " near offset "
										+ offset);
							}
							if (last instanceof NaryCriteria) {
								NaryCriteria criteria = (NaryCriteria) last;
								if (criteria.getSubCriteria().size() <= 1) {
									criteria.setOperator(operator);
								} else if (criteria.getOperator().equals(operator)) {
									// nothing to do
								} else {
									NaryCriteria newCriteria = new NaryCriteria();
									newCriteria.setOperator(operator);
									newCriteria.addSubCriteria(last);
									stack.remove(last);
									stack.add(newCriteria);
								}
							} else {
								NaryCriteria criteria = new NaryCriteria();
								criteria.setOperator(operator);
								criteria.addSubCriteria(last);
								stack.remove(last);
								stack.add(criteria);
							}
						} else {

							// expect column name
							String columnName = word;
							word = nextWord();
							if (word == null) {
								throw new IllegalStateException("Expected operator near index " + offset);
							}
							operator = Operator.fromQueryString(word);
							if (operator == null) {
								throw new IllegalStateException("Expected operator near index " + offset
										+ ": unexpected token " + word);
							}
							String value = nextWord();
							if (value == null) {
								throw new IllegalStateException("Expected literal near index " + offset);
							}
							Object literalValue;
							if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
								literalValue = value.substring(1, value.length() - 1);
							} else if (value.startsWith("date:")) {
								literalValue = new Date(Long.parseLong(value.substring("date:".length())));
							} else {
								try {
									literalValue = Integer.parseInt(value);
								} catch (NumberFormatException e) {
									throw new IllegalStateException("Illegal literal near offset " + offset + ": "
											+ word);
								}
							}
							ColumnCriteria columnCriteria = new ColumnCriteria(columnName, operator, literalValue);
							if (stack.isEmpty()) {
								stack.add(columnCriteria);
							} else {
								Criteria last = stack.get(stack.size() - 1);
								if (last instanceof NaryCriteria) {
									NaryCriteria nary = (NaryCriteria) last;
									if (nary.getOperator() == Operator.NOT) {
										if (nary.getSubCriteria().size() != 1) {
											throw new IllegalStateException("Operator " + nary.getOperator()
													+ " can only apply to one item");
										}
									}
									nary.addSubCriteria(columnCriteria);
								} else {
									throw new IllegalStateException("Illegal syntax near offset " + offset);
								}
							}
						}
					}
				}
			}
		}

		Criteria result = stack.isEmpty() ? null : stack.get(0);
		while (result instanceof NaryCriteria) {
			NaryCriteria nary = (NaryCriteria) result;
			if (nary.getSubCriteria().size() == 1) {
				result = nary.getSubCriteria().get(0);
			} else if (nary.getSubCriteria().isEmpty()) {
				result = null;
			} else {
				break;
			}
		}
		return result;
	}

	private String nextWord() {
		eatWhitespace();
		int mark = offset;
		int tail = offset;
		boolean quotedString = false;
		while (tail < text.length()) {
			char c = text.charAt(tail);
			if (c == '\'') {
				if (tail == mark) {
					quotedString = true;
				} else if (quotedString) {
					++tail;
					break;
				} else {
					throw new IllegalStateException("Unexpected character \'\'\' at offset " + tail);
				}
			} else {
				if (!quotedString) {
					if (c == '(' || c == ')') {
						if (tail == mark) {
							++tail;
						}
						break;
					} else if (isWhitespace(c)) {
						break;
					}
				} else {
					// in a string, continue
				}
			}
			++tail;
		}
		if (tail > mark) {
			offset = tail;
			return text.substring(mark, tail);
		}
		return null;
	}

	private void eatWhitespace() {
		while (offset < text.length()) {
			char c = text.charAt(offset);
			if (isWhitespace(c)) {
				++offset;
			} else {
				break;
			}
		}
	}

	private boolean isWhitespace(char c) {
		// can't use Character.isWhitespace here -- this is JavaScript after all
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}
}
