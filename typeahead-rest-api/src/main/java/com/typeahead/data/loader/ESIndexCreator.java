package com.typeahead.data.loader;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static com.typeahead.common.TypeaheadPropertyKeys.*;
import static com.typeahead.common.TypeaheadPropertyKeys.DEFAULT_SCHEME;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
class ESIndexCreator {
	private static final int ES_RETRY_COUNT = 10;
	private static final int RETRY_REQ_WAIT_INTERVAL = 2_000;
	private static final String CREATE_INDEX_JSON_PAYLOAD_FILE = "create_index_payload.json";

	private String esIndexName;
	private String esIndexEndPoint;

	private Environment env;

	public ESIndexCreator(Environment env) {
		String host = env.getProperty(ES_HOST, DEFAULT_HOST);
		String port = env.getProperty(ES_PORT, DEFAULT_PORT);
		String scheme = env.getProperty(ES_SCHEME, DEFAULT_SCHEME);

		esIndexName = env.getProperty(ES_INDEX, DEFAULT_ES_INDEX);
		esIndexEndPoint = scheme + "://" + host + ":" + port + "/" + esIndexName;
	}

	void createIndex() throws Exception {
		HttpPut httpPut = createIndexPutRequest();
		try (CloseableHttpClient httpClient = createDefault()) {
			for (int i = 0; i < ES_RETRY_COUNT; i++) {
				try {
					executeCreateIndexRequest(httpPut, httpClient);
					break;
				} catch (Exception e) {
					log.info("Tried " + i + " times. Will try again after " + RETRY_REQ_WAIT_INTERVAL + " msecs");
					Thread.sleep(RETRY_REQ_WAIT_INTERVAL);
					if (i == ES_RETRY_COUNT - 1) {
						throw e;
					}
				}
			}
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
		ClassPathResource resource = new ClassPathResource(CREATE_INDEX_JSON_PAYLOAD_FILE);
		try (Stream<String> lineStream = new BufferedReader(new InputStreamReader(resource.getInputStream())).lines()) {
			lineStream.forEach(sb::append);
		}
		return sb.toString();
	}
}
