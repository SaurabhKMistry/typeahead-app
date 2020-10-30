package com.typeahead.service;

import com.typeahead.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY_REDIS;

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
