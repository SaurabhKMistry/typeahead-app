package com.typeahead.redis;

import com.typeahead.ITypeaheadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT;
import static com.typeahead.redis.RedisPropertyKeys.REDIS_BATCH_SIZE;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
@Slf4j
public class RedisRepository implements ITypeaheadRepository {
	private static final String KEY_PREFIX = "TYP-";
	private static final String DEF_BATCH_SIZE = "10_000";

	private final StringRedisTemplate redisTemplate;
	private int batchSize;
	private List<Autocompletion> autocompletionList;
	private ZSetOperations<String, String> redisSortedSet;
	private Environment env;

	@Autowired
	public RedisRepository(StringRedisTemplate redisTemplate, Environment env) {
		this.redisTemplate = redisTemplate;
		this.redisSortedSet = redisTemplate.opsForZSet();
		this.env = env;
		this.batchSize = parseInt(env.getProperty(REDIS_BATCH_SIZE, DEF_BATCH_SIZE));
		this.autocompletionList = new ArrayList<>(batchSize);
	}

	public void cleanup() {
		if (!autocompletionList.isEmpty()) {
			executeBatch();
			autocompletionList.clear();
		}
	}

	public void executeBatch() {
		redisTemplate.execute((RedisConnection connection) -> {
			autocompletionList.forEach(autocompletion -> {
				byte[] key = autocompletion.getKey().getBytes();
				byte[] phrase = autocompletion.getPhrase().getBytes();
				double score = autocompletion.getScore();
				connection.zSetCommands().zAdd(key, score, phrase);
			});
			return null;
		}, false, true);
	}

	public void acceptPhrase(String phrase, double score) {
		if (isNotBlank(phrase)) {
			List<String> edgeNGrams = produceEdgeNGrams(phrase);
			edgeNGrams.forEach(nGram -> {
				String key = prepareKey(nGram);
				batchAddToSortedSet(new Autocompletion(key, phrase, score));
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

	private void batchAddToSortedSet(Autocompletion autocompletion) {
		autocompletionList.add(autocompletion);
		if (autocompletionList.size() >= batchSize) {
			executeBatch();
			autocompletionList.clear();
		}
	}

	@Override
	public Set<String> autocomplete(String prefix, int suggestionCount) {
		String count = env.getProperty(TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT, "5");
		int topSuggestionsToShowCount = parseInt(count);
		return redisSortedSet.reverseRange(prepareKey(prefix), 0,
										   topSuggestionsToShowCount);
	}
}
