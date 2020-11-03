package com.typeahead.trie;

import com.typeahead.ITypeaheadRepository;

import java.util.List;

public class TrieRepository implements ITypeaheadRepository {
	@Override
	public List<String> autocomplete(String prefix, int suggestionCount) {
		return null;
	}
}
