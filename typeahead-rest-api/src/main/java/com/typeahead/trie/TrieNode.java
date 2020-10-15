package com.typeahead.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Character.toLowerCase;

@Getter
@Setter
public class TrieNode {
	private char data;
	private int score;
	private boolean isEnd;
	private TrieNode parent;
	private LinkedList<TrieNode> children;

	public TrieNode(char c) {
		data = c;
		children = new LinkedList<>();
		isEnd = false;
	}

	public TrieNode getChild(char c) {
		if (children != null) {
			for (TrieNode eachChild : children)
				if (toLowerCase(eachChild.data) == toLowerCase(c)) {
					return eachChild;
				}
		}
		return null;
	}

	protected List<TrieNode> getWords() {
		List<TrieNode> list = new ArrayList<>();
		if (isEnd) {
			list.add(this);
		}

		if (children != null) {
			for (TrieNode child : children) {
				if (child != null) {
					list.addAll(child.getWords());
				}
			}
		}
		return list;
	}

	public String toString() {
		if (parent == null) {
			return "";
		} else {
			return parent.toString() + new String(new char[]{data});
		}
	}
}
