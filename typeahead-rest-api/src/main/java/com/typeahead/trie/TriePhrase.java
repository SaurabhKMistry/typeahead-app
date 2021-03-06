package com.typeahead.trie;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class TriePhrase {
	private static final int DEFAULT_SCORE = 0;
	@GeneratedValue
	@Id
	private long id;

	@NonNull private String word;
	@NonNull private int score;

	public TriePhrase(String word) {
		this(word, DEFAULT_SCORE);
	}
}
