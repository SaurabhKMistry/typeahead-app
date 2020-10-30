package com.typeahead.service;

import com.typeahead.repository.ESRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY_ES;

@Slf4j
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, matchIfMissing = false, havingValue = TYPEAHEAD_POWERED_BY_ES)
@Service
public class ESService implements ITypeaheadService {
	private ESRepository esRepo;

	public ESService(ESRepository esRepo) {
		this.esRepo = esRepo;
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		return esRepo.autocomplete(prefix, suggestionCount);
	}
}
