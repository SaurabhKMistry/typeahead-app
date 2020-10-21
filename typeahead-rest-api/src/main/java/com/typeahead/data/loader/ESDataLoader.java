package com.typeahead.data.loader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.typeahead.common.TypeaheadPropertyKeys.*;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
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
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, matchIfMissing = true, havingValue = TYPEAHEAD_POWERED_BY_ES)
public class ESDataLoader implements ITypeaheadDataLoader {
	private static final int DATA_FILES_COUNT = 10;
	private static final int DEFAULT_ES_WARM_UP_TIME = 10_000;
	private String esWarmupInterval;

	private ESIndexCreator esIndexCreator;
	private Environment env;

	@Autowired
	public ESDataLoader(Environment environment, ESIndexCreator esIndexCreator) {
		this.env = environment;
		this.esIndexCreator = esIndexCreator;
		esWarmupInterval = env.getProperty(ES_WARM_UP_INTERVAL, valueOf(DEFAULT_ES_WARM_UP_TIME));
	}

	@SneakyThrows
	public void loadData() {
		letESWarmup();
		esIndexCreator.createIndex();

		log.info("Starting Elastic Search Data Loader...");
		ExecutorService pool = Executors.newFixedThreadPool(DATA_FILES_COUNT);
		for (int i = 1; i <= DATA_FILES_COUNT; i++) {
			String csvFileName = "100K_names_" + i + ".csv";
			pool.submit(new ESDataLoaderTask(csvFileName, env));
		}
		letAllThreadsComplete(pool);
		log.info("Elastic Search Data Loader completed successfully...!!! You can use the system now");
	}

	private void letESWarmup() throws InterruptedException {
		log.info("Giving warm up time of " + esWarmupInterval + " to elastic search");
		sleep(parseLong(esWarmupInterval));
	}
}
