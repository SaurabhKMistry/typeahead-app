package com.auto.complete.typeahead.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
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
import java.util.ArrayList;
import java.util.List;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.apache.http.impl.client.HttpClients.createDefault;

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
		// sleep for 10 sec to allow elastic search to start up
		Thread.sleep(10_000);

		createIndexInElasticSearch();

		log.info("Starting to index elastic search data for the index [" + esIndexName + "]");
		HttpURLConnection conn = null;
		List<String> documentList = readFile();

		int documentBatchSize = 10_000;
		List<List<String>> partitionedList = partition(documentList, documentBatchSize);

		try {
			URL url = new URL(elasticsearchBulkEndpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			OutputStream os = conn.getOutputStream();

			for (List<String> columnSubList : partitionedList) {
				String s = String.join("", columnSubList);
				os.write(s.getBytes());
				os.flush();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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
		int retryCount = 10;
		String indexSettings = readCreateIndexPayloadFromFile();
		for (int i = 0; i < retryCount; i++) {
			try {
				CloseableHttpResponse httpResponse;
				try (CloseableHttpClient httpclient = createDefault()) {
					HttpPut httpPut = new HttpPut(esIndexEndPoint);
					httpPut.setHeader("Accept", "application/json");
					httpPut.setHeader("Content-type", "application/json");
					StringEntity stringEntity = new StringEntity(indexSettings);
					httpPut.setEntity(stringEntity);
					httpResponse = httpclient.execute(httpPut);
				}
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					log.info("Created index in elastic search");
					break;
				}
			} catch (Exception e) {
				// sleep for 2 secs before index creation.
				Thread.sleep(2_000);
			}
		}
	}

	/**
	 * Construct the BULK POST payload for Elastic Search
	 */
	private List<String> readFile() throws IOException {
		List<String> documentList = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			InputStream is = this.getClass().getResourceAsStream("/names_100000_data_" + i + ".csv");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] cols = line.split(",");
					String document = "{\"index\":{}} \n{\"name\":\"" + cols[7] + "\"} \n";
					documentList.add(document);
				}
			}
		}
		return documentList;
	}

	private String readCreateIndexPayloadFromFile() throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/create_index_payload.json");
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
}
