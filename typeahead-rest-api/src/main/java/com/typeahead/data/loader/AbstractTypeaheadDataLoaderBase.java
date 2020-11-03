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

	public abstract void loadData();

	protected abstract void initialize();

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
