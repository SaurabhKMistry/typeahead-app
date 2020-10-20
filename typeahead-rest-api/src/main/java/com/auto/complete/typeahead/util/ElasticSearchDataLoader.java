package com.auto.complete.typeahead.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.springframework.http.HttpMethod.POST;
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
	private static final int BETWEEN_REQ_SLEEP_INTERVAL = 2_000;
	private static final int NO_OF_ES_CONNECT_RETRY = 10;
	private static final int ES_WARM_UP_TIME = 10_000;
	private static final int DOCUMENT_BULK_BATCH_SIZE = 10_000;
	private static final String CREATE_INDEX_JSON_PAYLOAD_FILE = "src/main/resources/create_index_payload.json";

	@Autowired
	private Environment env;

	private String esIndexName;
	private String esIndexEndPoint;
	private String elasticsearchBulkEndpoint;

	@Autowired
	public ElasticSearchDataLoader(Environment env) {
		this.env = env;

		String host = env.getProperty(ES_HOST, DEFAULT_HOST);
		String port = env.getProperty(ES_PORT, DEFAULT_PORT);
		String scheme = env.getProperty(ES_SCHEME, DEFAULT_SCHEME);

		esIndexName = env.getProperty(ES_INDEX, DEFAULT_ES_INDEX);
		esIndexEndPoint = scheme + "://" + host + ":" + port + "/" + esIndexName;
		elasticsearchBulkEndpoint = esIndexEndPoint + "/_doc/_bulk";
	}

	public void loadDataFromFile() throws Exception {
		Thread.sleep(ES_WARM_UP_TIME);

		createIndexInElasticSearch();

		HttpURLConnection conn = null;
		List<String> documentList = readFile();
		List<List<String>> batchOfDocs = partition(documentList, DOCUMENT_BULK_BATCH_SIZE);

		try {
			URL url = new URL(elasticsearchBulkEndpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(POST.name());
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON_VALUE);
			OutputStream os = conn.getOutputStream();

			for (List<String> documents : batchOfDocs) {
				String bulkInsertDocPayload = String.join("", documents);
				os.write(bulkInsertDocPayload.getBytes());
				os.flush();
				if (conn.getResponseCode() != HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
			}
		} catch (Exception e) {
			log.error("Dataloader has failed to load elastic search data. Exception is - " + e.getMessage());
			// Do not stop. Log the error, Ignore the error for now and Continue
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		log.info("Completed indexing data for the index [" + esIndexName + "]");
	}

	private void createIndexInElasticSearch() throws Exception {
		HttpPut httpPut = new HttpPut(esIndexEndPoint);
		httpPut.setHeader(ACCEPT, APPLICATION_JSON_VALUE);
		httpPut.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		httpPut.setEntity(new StringEntity(getCreateIndexJsonPayload()));

		for (int i = 0; i < NO_OF_ES_CONNECT_RETRY; i++) {
			try {
				CloseableHttpResponse httpResponse;
				try (CloseableHttpClient httpclient = createDefault()) {
					httpResponse = httpclient.execute(httpPut);
				}
				if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
					log.info("Created index in elastic search");
					break;
				}
			} catch (Exception e) {
				log.info("Tried " + i + " times. Sleeping for 2 secs before trying again");
				Thread.sleep(BETWEEN_REQ_SLEEP_INTERVAL);
				if (i == NO_OF_ES_CONNECT_RETRY - 1) {
					throw e;
				}
			}
		}
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

	private String getCreateIndexJsonPayload() throws IOException {
		StringBuilder sb = new StringBuilder(500);
		try (Stream<String> lineStream = Files.lines(Path.of(CREATE_INDEX_JSON_PAYLOAD_FILE))) {
			lineStream.forEach(sb::append);
		}
		return sb.toString();
	}
}
