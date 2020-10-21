package com.typeahead.service;

import com.typeahead.trie.Trie;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY_TRIE;

@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_TRIE, matchIfMissing = false)
@Service
public class TrieService implements ITypeaheadService {
	private Trie trie;

	public TrieService(Trie trie) {
		this.trie = trie;
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		return trie.autocomplete(prefix, suggestionCount);
	}
}
