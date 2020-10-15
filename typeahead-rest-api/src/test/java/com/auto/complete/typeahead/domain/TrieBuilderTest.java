package com.auto.complete.typeahead.domain;

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
		List<TrieEntity> wordsInDB = of(new TrieEntity("word1"),
										new TrieEntity("word2"),
										new TrieEntity("word3"));
		doNothing().when(mockTrie).insert(anyString(), anyInt());

		trieBuilder.buildTrie(wordsInDB);

		verify(mockTrie, times(3)).insert(anyString(), anyInt());
	}
}