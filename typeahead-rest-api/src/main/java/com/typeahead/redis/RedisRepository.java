package com.typeahead.redis;

import com.typeahead.ITypeaheadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
@Slf4j
public class RedisRepository implements ITypeaheadRepository {
	private static final String KEY_PREFIX = "completion:";

	private ZSetOperations<String, String> redisSortedSet;

	private Environment env;

	@Autowired
	public RedisRepository(StringRedisTemplate stringRedisTemplate, Environment env) {
		this.redisSortedSet = stringRedisTemplate.opsForZSet();
		this.env = env;
	}

	public void acceptPhrase(String phrase, double score) {
		if (isNotBlank(phrase)) {
			List<String> edgeNGrams = produceEdgeNGrams(phrase);
			edgeNGrams.forEach(nGram -> {
				String key = prepareKey(nGram);
				redisSortedSet.add(key, phrase, score);
			});
		}
	}

	private List<String> produceEdgeNGrams(String phrase) {
		List<String> edgeNGrams = new ArrayList<>();
		phrase.chars()
			  .mapToObj(ch -> valueOf((char) ch))
			  .reduce((prevCharStr, currCharStr) -> {
				  edgeNGrams.add(prevCharStr);
				  return prevCharStr.concat(currCharStr);
			  });
		edgeNGrams.add(phrase);
		return edgeNGrams;
	}

	private String prepareKey(String nGram) {
		String key = KEY_PREFIX + nGram;
		return key.toLowerCase();
	}

	@Override
	public Set<String> autocomplete(String prefix, int suggestionCount) {
		String count = env.getProperty(TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT, "5");
		int topSuggestionsToShowCount = Integer.parseInt(count);
		return redisSortedSet.reverseRange(prepareKey(prefix), 0,
										   topSuggestionsToShowCount);
	}
}
