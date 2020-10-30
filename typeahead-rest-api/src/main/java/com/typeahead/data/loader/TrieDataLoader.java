package com.typeahead.data.loader;

import com.typeahead.trie.Trie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY_TRIE;

@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_TRIE, matchIfMissing = false)
public class TrieDataLoader implements ITypeaheadDataLoader {
	private Trie trie;
	private static final int DATA_FILES_COUNT = 10;

	@Autowired
	public TrieDataLoader(Trie trie) {
		this.trie = trie;
	}

	@Override
	public void loadData() {
		log.info("Starting Trie Data Loader...");
		for (int i = 1; i <= DATA_FILES_COUNT; i++) {
			String csvFileName = "100K_names_" + i + ".csv";
			ClassPathResource resource = new ClassPathResource(csvFileName);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
				Stream<String> lineStream = br.lines();
				lineStream.forEach(name -> trie.insert(name));
			} catch (IOException e) {
				log.error("Error while inserting data into trie. Error --> " + e.getMessage(), e);
				return;
			}
		}
		log.info("Trie Data Loader completed successfully loading [" + trie.size() + "] docs...!!! You can use the system now");
	}
}
