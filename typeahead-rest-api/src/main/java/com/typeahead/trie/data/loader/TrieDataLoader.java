package com.typeahead.trie.data.loader;

import com.typeahead.data.loader.AbstractTypeaheadDataLoader;
import com.typeahead.trie.Trie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_TRIE;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;

@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_TRIE)
public class TrieDataLoader extends AbstractTypeaheadDataLoader {
	private Trie trie;

	@Autowired
	public TrieDataLoader(Trie trie, Environment environment) {
		this.environment = environment;
		this.trie = trie;
	}

	@Override
	protected void initialize() {
		log.info("Starting Trie Data Loader...");
	}

	@Override
	protected void cleanup() {
		log.info("Trie Data Loader completed loading " + numDocLoaded + "] docs...!!! You can use the system now");
	}

	@Override
	protected Runnable getDataLoaderTask(String line) {
		return () -> trie.insert(line);
	}

	@Override
	protected void handleDataLoadException(Exception e, AtomicInteger numDocLoaded) {
		log.error("Error while loading data into trie. Error --> " + e.getMessage(), e);
	}
}
