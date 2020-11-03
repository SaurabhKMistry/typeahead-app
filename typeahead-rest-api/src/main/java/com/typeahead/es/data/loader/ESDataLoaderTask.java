package com.typeahead.es.data.loader;

import com.typeahead.es.common.ESConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
class ESDataLoaderTask implements Runnable {
	private static final int BULK_CREATE_REQ_BATCH_SIZE = 10_000;
	private static final int BULK_REQ_INTERVAL = 1_000;

	private final String dataFileName;

	private ESConfig esConfig;
	private HttpPost httpPost;

	public ESDataLoaderTask(String dataFileName, ESConfig esConfig) {
		this.dataFileName = dataFileName;
		this.esConfig = esConfig;
		this.httpPost = createHttpPost();
	}

	private HttpPost createHttpPost() {
		httpPost = new HttpPost(esConfig.getBulkCreateDocEndpoint());
		httpPost.setHeader(ACCEPT, APPLICATION_JSON_VALUE);
		httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		return httpPost;
	}

	@SneakyThrows
	@Override
	public void run() {
		Stream<String> lineStream = null;
		try {
			lineStream = getCSVFileLineStream();
			List<String> bulkCreateDocPayloadPerCSVLine = constructBulkCreateDocPayloadPerCSVLine(lineStream);
			List<List<String>> bulkCreateBatches = createBatches(bulkCreateDocPayloadPerCSVLine);
			for (List<String> bulkCreateBatch : bulkCreateBatches) {
				batchBulkCreateDoc(httpPost, bulkCreateBatch);
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

	private Stream<String> getCSVFileLineStream() throws IOException {
		ClassPathResource resource = new ClassPathResource(dataFileName);
		return new BufferedReader(new InputStreamReader(resource.getInputStream())).lines();
	}

	private List<String> constructBulkCreateDocPayloadPerCSVLine(Stream<String> lineStream) {
		return lineStream.map(line -> "{\"index\":{}} \n{\"name\":\"" + line + "\"} \n")
						 .collect(toList());
	}

	private List<List<String>> createBatches(List<String> createDocPayloads) {
		return partition(createDocPayloads, BULK_CREATE_REQ_BATCH_SIZE);
	}

	private void batchBulkCreateDoc(HttpPost httpPost, List<String> createDocPayloadBatch)
	throws IOException, InterruptedException {
		httpPost.setEntity(new StringEntity(String.join("", createDocPayloadBatch)));
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			httpClient.execute(httpPost);
			sleep(BULK_REQ_INTERVAL);
		}
	}

	void closeStream(Stream<String> stream) {
		if (stream != null) {
			stream.close();
		}
	}
}
