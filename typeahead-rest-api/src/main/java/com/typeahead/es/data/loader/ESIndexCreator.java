package com.typeahead.es.data.loader;

import com.typeahead.es.common.ESConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_ES;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_ES)
class ESIndexCreator {
	private static final int ES_RETRY_CREATE_INDEX_COUNT = 10;
	private static final int RETRY_REQ_WAIT_INTERVAL = 2_000;
	private static final String CREATE_INDEX_JSON_FILE = "create_index_payload.json";

	private ESConfig esConfig;
	private HttpPut httpPut;
	private CloseableHttpClient httpClient;

	@Autowired
	public ESIndexCreator(ESConfig esConfig) throws IOException {
		this.esConfig = esConfig;

		this.httpPut = createIndexPutRequest();
		this.httpClient = createDefault();
	}

	private HttpPut createIndexPutRequest() throws IOException {
		HttpPut httpPutObj = new HttpPut(esConfig.getCreateIndexEndpoint());
		httpPutObj.setHeader(ACCEPT, APPLICATION_JSON_VALUE);
		httpPutObj.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		httpPutObj.setEntity(new StringEntity(readCreateIndexJsonFile()));
		return httpPutObj;
	}

	private String readCreateIndexJsonFile() throws IOException {
		StringBuilder sb = new StringBuilder(500);
		ClassPathResource resource = new ClassPathResource(CREATE_INDEX_JSON_FILE);
		try (Stream<String> lineStream = new BufferedReader(new InputStreamReader(resource.getInputStream())).lines()) {
			lineStream.forEach(sb::append);
		}
		return sb.toString();
	}

	void createIndex() throws Exception {
		for (int i = 0; i < ES_RETRY_CREATE_INDEX_COUNT; i++) {
			if (createIndexInternal(httpClient, httpPut, i + 1)) {
				break;
			}
		}
	}

	private boolean createIndexInternal(CloseableHttpClient httpClient, HttpPut httpPutObj, int retryCount)
	throws InterruptedException, IOException {
		try {
			executeCreateIndexRequest(httpClient, httpPutObj);
			return true;
		} catch (IOException e) {
			log.info("Tried " + retryCount + " times. Will try again after " + RETRY_REQ_WAIT_INTERVAL + " msecs");
			Thread.sleep(RETRY_REQ_WAIT_INTERVAL);
			if (retryCount == ES_RETRY_CREATE_INDEX_COUNT - 1) {
				throw e;
			}
		}
		return false;
	}

	private void executeCreateIndexRequest(CloseableHttpClient httpClient, HttpPut httpPut)
	throws IOException {
		String indexName = esConfig.getESIndexName();
		log.info("Creating index [" + indexName + "]");
		CloseableHttpResponse httpResponse = httpClient.execute(httpPut);
		if (httpResponse.getStatusLine().getStatusCode() == SC_OK) {
			log.info("Index [" + indexName + "] created successfully");
		} else {
			throw new BeanInitializationException("Index [" + indexName + "] already exists!");
		}
	}
}
