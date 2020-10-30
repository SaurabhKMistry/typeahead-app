package com.typeahead.redis;

import lombok.*;

import java.io.Serializable;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Autocompletion implements Serializable {
	private static final int DEFAULT_SCORE = 1;

	@NonNull
	private String phrase;

	private int score = DEFAULT_SCORE;
}
