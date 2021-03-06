package com.typeahead.common;

public final class TypeaheadPropertyKeys {
	// Typeahead API error message keys
	public static final String INVALID_SUGGESTION_COUNT = "invalid.suggestion.count";
	public static final String MISSING_REQUIRED_REQ_BODY = "missing.required.request.body";
	public static final String MISSING_KEY_IN_REQUEST_BODY = "missing.key.in.request.body";
	public static final String PREFIX_QRY_PARAM_MISSING = "required.qry.param.missing";
	public static final String DECORATED_PREFIX_QRY_PARAM_MISSING = "{required.qry.param.missing}";
	public static final String DECORATED_INVALID_SUGGESTION_COUNT = "{invalid.suggestion.count}";

	// Configurable Typeahead Application property keys
	public static final String DEF_SUGGESTION_FETCH_COUNT = "${default.suggestion.fetch.count}";
	public static final String CROSS_ORIGIN_TARGET = "cross.origin.target";
	public static final String TYPEAHEAD_POWERED_BY = "typeahead.powered.by";
	public static final String TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT = "typeahead.top.suggestions.to.show.count";
	public static final String DATA_FILES_COUNT = "typeahead.data.files.count";

	private TypeaheadPropertyKeys() {
	}
}
