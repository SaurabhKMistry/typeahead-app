package com.typeahead.data.loader;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;

public interface ITypeaheadDataLoader {
	void loadData();

	default void letAllThreadsComplete(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(2, MINUTES)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
