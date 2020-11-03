package com.typeahead.trie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrieBuilder {
	Trie trie;

	@Autowired
	public TrieBuilder(Trie trie) {
		this.trie = trie;
	}

	public void buildTrie(Iterable<TriePhrase> entities) {
		entities.forEach(trieEntity -> trie.insert(trieEntity.getWord(), trieEntity.getScore()));
	}
}
