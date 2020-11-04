package com.typeahead.redis;

import com.typeahead.data.loader.AbstractTypeaheadDataLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_REDIS;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT;

@Slf4j
@Component
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_REDIS)
public class RedisDataLoader extends AbstractTypeaheadDataLoader {
	private static final double DEFAULT_SCORE = 1.0;

	protected RedisRepository redisRepo;

	@Autowired
	public RedisDataLoader(RedisRepository redisRepository, Environment environment) {
		this.environment = environment;
		this.redisRepo = redisRepository;
	}

	@Override
	protected boolean initialize() {
		String suggestionsToShowCountStr = environment.getProperty(TYPEAHEAD_TOP_SUGGESTION_TO_SHOW_COUNT);
		Set<String> autoSuggestions = redisRepo.autocomplete("a", 1);
		if(autoSuggestions != null && !autoSuggestions.isEmpty()){
			return false;
		}
		log.info("Starting Redis Data Loader...");
		return true;
	}

	@Override
	protected void cleanup() {
		redisRepo.cleanup();
		log.info("Redis Data Loader completed loading " + numDocLoaded + " docs...!!! You can use the system now");
	}

	@Override
	protected Runnable getDataLoaderTask(String line) {
		return () -> {
			redisRepo.acceptPhrase(line, DEFAULT_SCORE);
		};
	}

	@Override
	protected void handleDataLoadException(Exception e, AtomicInteger numDocLoaded) {
		log.info("Error after loading [" + numDocLoaded + "] docs. Error --> " + e.getMessage(), e);
	}
}
