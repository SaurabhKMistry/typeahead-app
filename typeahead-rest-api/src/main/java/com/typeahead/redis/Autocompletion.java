package com.typeahead.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Autocompletion {
	private String key;
	private String phrase;
	private double score;
}
