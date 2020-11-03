package com.typeahead.redis;

import com.typeahead.ITypeaheadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_REDIS;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;

@Slf4j
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, matchIfMissing = true, havingValue = TYPEAHEAD_POWERED_BY_REDIS)
@Service
public class RedisService implements ITypeaheadService {
	private RedisRepository redisRepo;

	public RedisService(RedisRepository redisRepo) {
		this.redisRepo = redisRepo;
	}

	public Collection<String> autocomplete(String prefix, int suggestionCount) {
		return redisRepo.autocomplete(prefix, suggestionCount);
	}
}
