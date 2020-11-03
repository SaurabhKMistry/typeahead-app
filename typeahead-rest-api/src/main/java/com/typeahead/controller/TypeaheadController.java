package com.typeahead.controller;

import com.typeahead.ITypeaheadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;

import static com.typeahead.common.TypeaheadPropertyKeys.*;
import static java.util.Collections.emptyList;

@Slf4j
@RestController
@CrossOrigin(value = "${" + CROSS_ORIGIN_TARGET + "}")
@Validated
public class TypeaheadController {
	private ITypeaheadService typeaheadService;

	@Autowired
	public TypeaheadController(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
									   ITypeaheadService typeaheadService) {
		log.info("Typeahead service is " + typeaheadService.getClass().getName());
		this.typeaheadService = typeaheadService;
	}

	@GetMapping("/typeahead")
	public Collection<String> getAutoSuggestions(@RequestParam
												 @NotNull(message = DECORATED_PREFIX_QRY_PARAM_MISSING) String prefix,
												 @RequestParam(defaultValue = DEF_SUGGESTION_FETCH_COUNT)
												 @Min(value = 1, message = DECORATED_INVALID_SUGGESTION_COUNT) int suggestionCount) {
		Collection<String> autocompleteList = typeaheadService.autocomplete(prefix, suggestionCount);
		return autocompleteList == null ? emptyList() : autocompleteList;
	}
}
