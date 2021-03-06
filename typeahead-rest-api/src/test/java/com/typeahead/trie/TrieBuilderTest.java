package com.typeahead.trie;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TrieBuilderTest {
	private static Trie mockTrie = mock(Trie.class);
	private static TrieBuilder trieBuilder;

	@BeforeAll
	static void beforeEach() {
		trieBuilder = new TrieBuilder(mockTrie);
	}

	@Test
	void buildTrie() {
		List<TriePhrase> phrasesInDB = of(new TriePhrase("word1"),
										  new TriePhrase("word2"),
										  new TriePhrase("word3"));
		doNothing().when(mockTrie).insert(anyString(), anyInt());

		trieBuilder.buildTrie(phrasesInDB);

		verify(mockTrie, times(3)).insert(anyString(), anyInt());
	}
}