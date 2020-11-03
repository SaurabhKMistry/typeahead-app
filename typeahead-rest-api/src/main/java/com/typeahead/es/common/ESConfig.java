package com.typeahead.es.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.typeahead.common.TypeaheadPropertyKeys.*;
import static java.lang.String.valueOf;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Component
@Scope(value = SCOPE_SINGLETON)
public class ESConfig {
	private static final String DEFAULT_ES_INDEX = "typeahead";
	private static final String DEFAULT_ES_FIELD = "name.completion";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "9200";
	private static final String DEFAULT_SCHEME = "http";

	private static final int DEFAULT_ES_WARM_UP_TIME = 10_000;

	private Environment environment;

	@Autowired
	public ESConfig(Environment environment) {
		this.environment = environment;
	}

	public String getESCompletionFieldName() {
		return environment.getProperty(ES_FIELD_NAME, DEFAULT_ES_FIELD);
	}

	public String getBulkCreateDocEndpoint() {
		return getCreateIndexEndpoint() + "/_doc/_bulk";
	}

	public String getCreateIndexEndpoint() {
		return getScheme() + "://" + getHost() + ":" + getPort() + "/" + getESIndexName();
	}

	public String getScheme() {
		return environment.getProperty(ES_SCHEME, DEFAULT_SCHEME);
	}

	public String getHost() {
		return environment.getProperty(ES_HOST, DEFAULT_HOST);
	}

	public String getPort() {
		return environment.getProperty(ES_PORT, DEFAULT_PORT);
	}

	public String getESIndexName() {
		return environment.getProperty(ES_INDEX, DEFAULT_ES_INDEX);
	}

	public String getESWarmupInterval() {
		return environment.getProperty(ES_WARM_UP_INTERVAL, valueOf(DEFAULT_ES_WARM_UP_TIME));
	}
}
