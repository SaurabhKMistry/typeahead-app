package com.typeahead.trie;

import com.typeahead.ITypeaheadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_TRIE;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;

@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_TRIE)
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
