package com.auto.complete.typeahead.controller;

import com.auto.complete.typeahead.service.ElasticSearchService;
import com.auto.complete.typeahead.service.TrieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.util.Collections.emptyList;

@Slf4j
@RestController
@CrossOrigin(value = "${" + CROSS_ORIGIN_TARGET + "}")
@Validated
public class TypeaheadController {
	private ElasticSearchService esService;
	private TrieService trieService;

	@Autowired
	public TypeaheadController(ElasticSearchService esService) {
		this.esService = esService;
	}

	@GetMapping("/typeahead")
	public List<String> getAutoSuggestions(@RequestParam
										   @NotNull(message = DECORATED_PREFIX_QRY_PARAM_MISSING) String prefix,
										   @RequestParam(defaultValue = DEF_SUGGESTION_FETCH_COUNT)
										   @Min(value = 1, message = DECORATED_INVALID_SUGGESTION_COUNT) int suggestionCount) {
		List<String> autocompleteList = esService.autocomplete(prefix, suggestionCount);
		return autocompleteList == null ? emptyList() : autocompleteList;
	}

	@GetMapping("/trie/typeahead")
	public List<String> getTrieAutoSuggestions(@RequestParam
											   @NotNull(message = DECORATED_PREFIX_QRY_PARAM_MISSING) String prefix,
											   @RequestParam(defaultValue = DEF_SUGGESTION_FETCH_COUNT)
											   @Min(value = 1, message = DECORATED_INVALID_SUGGESTION_COUNT) int suggestionCount) {
		List<String> autocompleteList = trieService.autocomplete(prefix, suggestionCount);
		return autocompleteList == null ? emptyList() : autocompleteList;
	}
}
