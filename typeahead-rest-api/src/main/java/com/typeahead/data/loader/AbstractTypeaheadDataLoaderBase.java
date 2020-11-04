package com.typeahead.data.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import static com.typeahead.common.TypeaheadPropertyKeys.DATA_FILES_COUNT;
import static java.lang.Integer.parseInt;

@Slf4j
public abstract class AbstractTypeaheadDataLoaderBase {
	protected Environment environment;

	protected String getCsvFileName(int index) {
		return "100K_names_" + index + ".csv";
	}

	public void loadData() {
		long t1 = System.currentTimeMillis();
		if (initialize()) {
			startLoading();
			cleanup();
		}
		long t2 = System.currentTimeMillis();
		log.info("Time taken for complete data load is " + (t2 - t1) / 1000 + " secs");
	}

	protected abstract boolean initialize();

	protected abstract void startLoading();

	protected abstract void cleanup();

	protected int getDataFileCount() {
		int dataFileCount = 0;
		String dataFilesCountStr = environment.getProperty(DATA_FILES_COUNT);
		try {
			dataFileCount = parseInt(dataFilesCountStr != null ? dataFilesCountStr : "0");
		} catch (NumberFormatException e) {
			// Log and swallow the exception and return data file count as ZERO
			log.error("Property [" + DATA_FILES_COUNT + "] is not configured properly");
		}
		return dataFileCount;
	}
}
