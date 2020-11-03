package com.typeahead.data.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractTypeaheadDataLoader extends AbstractTypeaheadDataLoaderBase {
	private static final int PUBLISH_STATUS_AFTER_DOCS = 10_000;

	protected AtomicInteger numDocLoaded = new AtomicInteger();

	@Override
	public void loadData() {
		initialize();
		startLoading();
		cleanup();
	}

	protected void startLoading() {
		int dataFileCount = getDataFileCount();
		for (int i = 1; i <= dataFileCount; i++) {
			try {
				String csvDataFileName = getCsvFileName(i);
				processCSVDataFile(csvDataFileName);
			} catch (Exception e) {
				handleDataLoadException(e, numDocLoaded);
				return;
			}
		}
	}

	protected abstract void cleanup();

	private void processCSVDataFile(String csvDataFileName) throws IOException {
		ClassPathResource csvDataFileResource = new ClassPathResource(csvDataFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(csvDataFileResource.getInputStream()));
		br.lines().forEach(this::processCSVDataFileLine);
	}

	protected abstract void handleDataLoadException(Exception e, AtomicInteger numDocLoaded);

	private void processCSVDataFileLine(String line) {
		numDocLoaded.incrementAndGet();
		if (numDocLoaded.get() % PUBLISH_STATUS_AFTER_DOCS == 0) {
			log.info("Loaded " + numDocLoaded.get() + " documents.");
		}
		getDataLoaderTask(line).run();
	}

	protected abstract Runnable getDataLoaderTask(String line);
}
