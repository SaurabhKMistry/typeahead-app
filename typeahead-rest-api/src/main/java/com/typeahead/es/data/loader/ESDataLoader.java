package com.typeahead.es.data.loader;

import com.typeahead.data.loader.AbstractTypeaheadConcurrentDataLoader;
import com.typeahead.es.common.ESConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_ES;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static java.lang.Long.parseLong;
import static java.lang.Thread.sleep;

/**
 * This class is responsible to load historical typeahead data from flat-file to persistence layer.
 * names_1m_data.csv is sample file that contains 1Million name suggestions.
 * This class has methods to read this file, extract the relevant columns.
 * After which if will do a bulk POST to Elastic Search using Elastic Search rest endpoints.
 * This class is called o Spring Boot application startup inorder to ensure that there is enough data indexed in Elastic Search.
 * As there are 1M records to index, the application indexes 10000 documents at a time in 100 batches.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_ES)
public class ESDataLoader extends AbstractTypeaheadConcurrentDataLoader {
	private ESIndexCreator esIndexCreator;

	@Autowired
	public ESDataLoader(ESConfig esConfig, ESIndexCreator esIndexCreator, Environment environment) {
		this.environment = environment;
		this.esConfig = esConfig;
		this.esIndexCreator = esIndexCreator;
	}

	@SneakyThrows
	@Override
	public void initialize() {
		threadPool = Executors.newFixedThreadPool(getDataFileCount());
		waitUntilESStarted();
		esIndexCreator.createIndex();
	}

	@Override
	public String getDataLoaderDisplayName() {
		return "Elastic Search Data Loader";
	}

	@Override
	public Runnable getDataLoaderTask(String csvFileName, ESConfig esConfig) {
		return new ESDataLoaderTask(csvFileName, esConfig);
	}

	private void waitUntilESStarted() throws InterruptedException {
		log.info("Let elastic search warm up for " + esConfig.getESWarmupInterval() + " ms before bulk loading data");
		sleep(parseLong(esConfig.getESWarmupInterval()));
	}
}
