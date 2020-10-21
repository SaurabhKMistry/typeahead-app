package com.typeahead.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.typeahead.trie.Trie.DEFAULT_SCORE;
import static org.junit.jupiter.api.Assertions.*;

class TrieTest {
	private Trie trie;
	private String word = "any_word";

	@BeforeEach
	void setUp() {
		trie = new Trie();
	}

	@Test
	void test_insert_single_word_into_trie_with_default_score() {
		trie.insert(word);

		Optional<TrieNode> trieNode = trie.search(word);
		assertTrue(trieNode.isPresent());
		assertEquals(DEFAULT_SCORE, trieNode.get().getScore());
	}

	@Test
	void test_insert_single_word_into_trie_with_a_score() {
		int score = 10;

		trie.insert(word, score);
		Optional<TrieNode> trieNode = trie.search(word);
		assertTrue(trieNode.isPresent());
		assertEquals(score, trieNode.get().getScore());
	}

	@Test
	void test_autocomplete_suggestions_are_sorted_by_score() {
		String wordWithScore1 = "wordWithScore1";
		String wordWithScore2 = "wordWithScore2";
		String wordWithScore5 = "wordWithScore5";

		trie.insert(wordWithScore5, 5);
		trie.insert(wordWithScore1, 1);
		trie.insert(wordWithScore2, 2);

		List<String> words = trie.autocomplete("word", 5);

		assertNotNull(words);
		assertEquals(3, words.size());
		assertEquals(wordWithScore5, words.get(0));
		assertEquals(wordWithScore2, words.get(1));
		assertEquals(wordWithScore1, words.get(2));
	}

	@Test
	void test_return_empty_autcomplete_suggestions_when_prefix_does_not_match_any_words() {
		trie.insert(word);

		List<String> words = trie.autocomplete("some_prefix_that_does_not_match_with_any_words", 2);

		assertNotNull(words);
		assertEquals(0, words.size());
	}

	@Test
	void test_return_only_suggestion_count_number_of_autocomplete_words() {
		String wordWithScore1 = "wordWithScore1";
		String wordWithScore2 = "wordWithScore2";
		String wordWithScore5 = "wordWithScore5";

		trie.insert(wordWithScore5);
		trie.insert(wordWithScore1);
		trie.insert(wordWithScore2);

		List<String> words = trie.autocomplete("word", 1);

		assertNotNull(words);
		assertEquals(1, words.size());
	}
}