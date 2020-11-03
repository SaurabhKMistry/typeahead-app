package com.typeahead.es;

import com.typeahead.ITypeaheadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_ES;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;

@Slf4j
@ConditionalOnProperty(name = TYPEAHEAD_POWERED_BY, havingValue = TYPEAHEAD_POWERED_BY_ES)
@Service
public class ESService implements ITypeaheadService {
	private ESRepository esRepo;

	@Autowired
	public ESService(ESRepository esRepo) {
		this.esRepo = esRepo;
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		return esRepo.autocomplete(prefix, suggestionCount);
	}
}
