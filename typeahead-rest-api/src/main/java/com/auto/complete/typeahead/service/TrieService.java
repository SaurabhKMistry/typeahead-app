package com.auto.complete.typeahead.service;

import com.auto.complete.typeahead.domain.Trie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrieService {
	private Trie trie;

	@Autowired
	public TrieService(Trie trie) {
		this.trie = trie;
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		return trie.autocomplete(prefix, suggestionCount);
	}
}
