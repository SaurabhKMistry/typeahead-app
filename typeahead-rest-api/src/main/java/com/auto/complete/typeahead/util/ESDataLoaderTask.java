package com.auto.complete.typeahead.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ESDataLoaderTask implements Runnable {
	private static final int BULK_CREATE_REQ_BATCH_SIZE = 10_000;
	private static final int BULK_REQ_INTERVAL = 1_000;

	private final String dataFileName;
	private final String esBulkEndpoint;

	public ESDataLoaderTask(String dataFileName, Environment env) {
		this.dataFileName = dataFileName;

		String host = env.getProperty(ES_HOST, DEFAULT_HOST);
		String port = env.getProperty(ES_PORT, DEFAULT_PORT);
		String scheme = env.getProperty(ES_SCHEME, DEFAULT_SCHEME);
		String esIndexName = env.getProperty(ES_INDEX, DEFAULT_ES_INDEX);

		String esIndexEndpoint = scheme + "://" + host + ":" + port + "/" + esIndexName;
		esBulkEndpoint = esIndexEndpoint + "/_doc/_bulk";
	}

	@SneakyThrows
	@Override
	public void run() {
		Stream<String> lineStream = null;
		try {
			lineStream = getCreateDocPayloadLineStream();
			HttpPost httpPost = createHttpPost();

			List<List<String>> createDocPayloadBatches = createBatches(getCreateDocPayloadList(lineStream));
			for (List<String> createDocPayloadBatch : createDocPayloadBatches) {
				httpPost.setEntity(new StringEntity(String.join("", createDocPayloadBatch)));
				try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
					httpClient.execute(httpPost);
					sleep(BULK_REQ_INTERVAL);
				}
			}
		} catch (InterruptedException e) {
			// DO NOTHING
		} catch (Exception e) {
			log.error("Error bulk uploading documents. Error --> " + e.getMessage(), e);
			throw e;
		} finally {
			closeStream(lineStream);
		}
	}

	private Stream<String> getCreateDocPayloadLineStream() throws IOException {
		ClassPathResource resource = new ClassPathResource(dataFileName);
		return new BufferedReader(new InputStreamReader(resource.getInputStream())).lines();
	}

	private List<String> getCreateDocPayloadList(Stream<String> lineStream) {
		return lineStream.map(line -> "{\"index\":{}} \n{\"name\":\"" + line + "\"} \n")
						 .collect(toList());
	}

	private List<List<String>> createBatches(List<String> createDocPayloads) {
		return partition(createDocPayloads, BULK_CREATE_REQ_BATCH_SIZE);
	}

	private HttpPost createHttpPost() {
		HttpPost httpPost = new HttpPost(esBulkEndpoint);
		httpPost.setHeader(ACCEPT, APPLICATION_JSON_VALUE);
		httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		return httpPost;
	}

	private void closeStream(Stream<String> stream) {
		if (stream != null) {
			stream.close();
		}
	}
}
