package com.auto.complete.typeahead;

public final class TypeaheadPropertyKeys {
	// Typeahead API error message constants
	public static final String INVALID_SUGGESTION_COUNT = "invalid.suggestion.count";
	public static final String MISSING_REQUIRED_REQ_BODY = "missing.required.request.body";
	public static final String MISSING_KEY_IN_REQUEST_BODY = "missing.key.in.request.body";
	public static final String PREFIX_QRY_PARAM_MISSING = "required.qry.param.missing";
	public static final String DECORATED_PREFIX_QRY_PARAM_MISSING = "{required.qry.param.missing}";
	public static final String DECORATED_INVALID_SUGGESTION_COUNT = "{invalid.suggestion.count}";

	// Default typeahead configurable value constants
	public static final String DEF_SUGGESTION_FETCH_COUNT = "${default.suggestion.fetch.count}";
	public static final String CROSS_ORIGIN_TARGET = "cross.origin.target";

	// Elastis search configuable property key constants
	public static final String ES_HOST = "elastic.search.host";
	public static final String ES_PORT = "elastic.search.port";
	public static final String ES_SCHEME = "elastic.search.scheme";
	public static final String ES_INDEX = "elastic.search.index";
	public static final String ES_FIELD_NAME = "elastic.search.field.name";

	// Elastis search default configuable property value constants
	public static final String DEFAULT_HOST = "localhost";
	public static final String DEFAULT_PORT = "9200";
	public static final String DEFAULT_SCHEME = "http";
	public static final String DEFAULT_ES_INDEX = "typeahead";
	public static final String DEFAULT_ES_FIELD = "name.completion";

	private TypeaheadPropertyKeys() {
	}
}
