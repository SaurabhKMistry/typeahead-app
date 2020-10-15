package com.auto.complete.typeahead.service;

import com.auto.complete.typeahead.repository.ElasticSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ElasticSearchService implements ITypeaheadService {
	private ElasticSearchRepository esRepo;

	@Autowired
	public ElasticSearchService(ElasticSearchRepository esRepo) {
		this.esRepo = esRepo;
	}

	public List<String> autocomplete(String prefix, int suggestionCount) {
		return esRepo.autocomplete(prefix, suggestionCount);
	}
}
