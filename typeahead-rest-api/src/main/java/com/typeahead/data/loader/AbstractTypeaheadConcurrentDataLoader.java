package com.typeahead.data.loader;

import com.typeahead.es.common.ESConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
public abstract class AbstractTypeaheadConcurrentDataLoader extends AbstractTypeaheadDataLoaderBase {
	private static final int AWAIT_TERMINATION_TIMEOUT_IN_MINS = 2;

	protected ESConfig esConfig;

	protected ExecutorService threadPool;

	@Override
	public void startLoading() {
		log.info("Starting " + getDataLoaderDisplayName() + "...");
		int dataFileCount = getDataFileCount();
		ExecutorService pool = Executors.newFixedThreadPool(dataFileCount);
		for (int i = 1; i <= dataFileCount; i++) {
			String csvFileName = "100K_names_" + i + ".csv";
			pool.submit(getDataLoaderTask(csvFileName, esConfig));
		}
	}

	@Override
	public void cleanup() {
		waitUntilAllThreadsCompleted();
		log.info(getDataLoaderDisplayName() + " completed successfully...!!! You can use the system now");
	}

	public abstract String getDataLoaderDisplayName();

	public abstract Runnable getDataLoaderTask(String csvFileName, ESConfig esConfig);

	protected void waitUntilAllThreadsCompleted() {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(AWAIT_TERMINATION_TIMEOUT_IN_MINS, MINUTES)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			currentThread().interrupt();
		}
	}
}
