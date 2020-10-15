package com.typeahead.trie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class Trie implements Comparator<TrieNode> {
	private static final char ROOT_NODE_DATA = ' ';
	public static final int DEFAULT_SCORE = 1;
	private TrieNode root;
	private int size = 0;

	public Trie() {
		root = new TrieNode(ROOT_NODE_DATA);
	}

	public void insert(String word) {
		insert(word, DEFAULT_SCORE);
	}

	public void insert(String word, int score) {
		Optional<TrieNode> node = search(word);
		node.orElseGet(() -> {
			TrieNode curr = root;
			TrieNode prev;
			for (char ch : word.toCharArray()) {
				prev = curr;
				TrieNode child = curr.getChild(ch);
				if (child != null) {
					curr = child;
					child.setParent(prev);
				} else {
					curr.getChildren().add(new TrieNode(ch));
					curr = curr.getChild(ch);
					curr.setParent(prev);
				}
			}
			curr.setEnd(true);
			curr.setScore(score);
			size += 1;
			return curr;
		});
	}

	public int size(){
		return size;
	}

	public Optional<TrieNode> search(String word) {
		TrieNode current = root;

		for (char ch : word.toCharArray()) {
			if (current.getChild(ch) == null) {
				return Optional.empty();
			} else {
				current = current.getChild(ch);
			}
		}
		if (current.isEnd()) {
			return Optional.of(current);
		}
		return Optional.empty();
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		TrieNode lastNode = root;
		for (int i = 0; i < prefix.length(); i++) {
			lastNode = lastNode.getChild(prefix.charAt(i));
			if (lastNode == null) {
				return new ArrayList<>();
			}
		}
		List<TrieNode> suggestions = lastNode.getWords();
		suggestions.sort(this);

		if (suggestions.size() > suggestionCount) {
			suggestions = suggestions.subList(0, suggestionCount);
		}
		return suggestions.stream()
						  .map(TrieNode::toString)
						  .collect(toList());
	}

	@Override
	public int compare(TrieNode o1, TrieNode o2) {
		return o2.getScore() - o1.getScore();
	}
}
