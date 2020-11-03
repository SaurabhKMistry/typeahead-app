package com.typeahead.common;

public final class TypeaheadConstants {
	public static final String TYPEAHEAD_POWERED_BY_ES = "Elastic-Search";
	public static final String TYPEAHEAD_POWERED_BY_REDIS = "Redis";
	public static final String TYPEAHEAD_POWERED_BY_TRIE = "Trie";

	private TypeaheadConstants() throws IllegalAccessException {
		throw new IllegalAccessException("An attempt is made to create an instance of ["
												 + getClass().getName() + "] through its private constructor.");
	}
}
