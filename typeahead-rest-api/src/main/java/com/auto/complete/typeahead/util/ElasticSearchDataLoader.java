package com.auto.complete.typeahead.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
/**
 * This class is responsible to load historical typeahead data from flat-file to persistence layer.
 * names_1m_data.csv is sample file that contains 1Million name suggestions.
 * This class has methods to read this file, extract the relevant columns.
 * After which if will do a bulk POST to Elastic Search using Elastic Search rest endpoints.
 * This class is called o Spring Boot application startup inorder to ensure that there is enough data indexed in Elastic Search.
 * As there are 1M records to index, the application indexes 10000 documents at a time in 100 batches.
 */
@Component
public class ElasticSearchDataLoader {
	private static final int NO_OF_DATA_FILES = 10;
	private static final int REQ_INTERVAL = 2_000;
	private static final int NO_OF_ES_CONNECT_RETRY = 10;
	private static final int DEFAULT_ES_WARM_UP_TIME = 10_000;
	private static final String CREATE_INDEX_JSON_PAYLOAD_FILE = "src/main/resources/create_index_payload.json";

	private String esIndexName;
	private String esIndexEndPoint;

	private Environment env;

	private String esWarmupInterval;

	@Autowired
	public ElasticSearchDataLoader(Environment environment) {
		this.env = environment;

		String host = env.getProperty(ES_HOST, DEFAULT_HOST);
		String port = env.getProperty(ES_PORT, DEFAULT_PORT);
		String scheme = env.getProperty(ES_SCHEME, DEFAULT_SCHEME);

		esIndexName = env.getProperty(ES_INDEX, DEFAULT_ES_INDEX);
		esIndexEndPoint = scheme + "://" + host + ":" + port + "/" + esIndexName;
		esWarmupInterval = env.getProperty(ES_WARM_UP_INTERVAL, valueOf(DEFAULT_ES_WARM_UP_TIME));
	}

	@SneakyThrows
	public void loadDataInElasticSearch() {
		log.info("Giving warm up time of " + esWarmupInterval + " to elastic search");
		Thread.sleep(parseLong(esWarmupInterval));
		createIndexInElasticSearch();

		log.info("Starting data load in elastic search...");
		ExecutorService pool = Executors.newFixedThreadPool(NO_OF_DATA_FILES);
		for (int i = 1; i <= NO_OF_DATA_FILES; i++) {
			String csvFileName = "src/main/resources/100K_names_" + i + ".csv";
			pool.submit(new ESDataLoaderTask(csvFileName, env));
		}
		awaitTerminationAfterShutdown(pool);
		log.info("Data load complete. Thread pool is terminated");
	}

	private void createIndexInElasticSearch() throws Exception {
		HttpPut httpPut = createIndexPutRequest();
		try (CloseableHttpClient httpClient = createDefault()) {
			for (int i = 0; i < NO_OF_ES_CONNECT_RETRY; i++) {
				try {
					executeCreateIndexRequest(httpPut, httpClient);
					break;
				} catch (Exception e) {
					log.info("Tried " + i + " times. Sleeping for " + REQ_INTERVAL + " msecs before trying again");
					Thread.sleep(REQ_INTERVAL);
					if (i == NO_OF_ES_CONNECT_RETRY - 1) {
						throw e;
					}
				}
			}
		}
	}

	public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(60, SECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private HttpPut createIndexPutRequest() throws IOException {
		HttpPut httpPut = new HttpPut(esIndexEndPoint);
		httpPut.setHeader(ACCEPT, APPLICATION_JSON_VALUE);
		httpPut.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		httpPut.setEntity(new StringEntity(getCreateIndexJsonPayload()));
		return httpPut;
	}

	private void executeCreateIndexRequest(HttpPut httpPut, CloseableHttpClient httpClient)
	throws IOException {
		log.info("Creating index [" + esIndexName + "]");
		CloseableHttpResponse httpResponse = httpClient.execute(httpPut);
		if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
			log.info("Index [" + esIndexName + "] created successfully");
		}
	}

	private String getCreateIndexJsonPayload() throws IOException {
		StringBuilder sb = new StringBuilder(500);
		try (Stream<String> lineStream = Files.lines(Path.of(CREATE_INDEX_JSON_PAYLOAD_FILE))) {
			lineStream.forEach(sb::append);
		}
		return sb.toString();
	}

	/**
	 * Construct the BULK POST payload for Elastic Search
	 */
	private List<String> readFile() throws IOException {
		List<String> documentList = new ArrayList<>();
		for (int i = 1; i <= NO_OF_DATA_FILES; i++) {
			String csvFileName = "/100K_names_" + i + ".csv";
			InputStream is = this.getClass().getResourceAsStream(csvFileName);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line;
				while ((line = br.readLine()) != null) {
					String document = "{\"index\":{}} \n{\"name\":\"" + line + "\"} \n";
					documentList.add(document);
				}
			}
		}
		return documentList;
	}
}
