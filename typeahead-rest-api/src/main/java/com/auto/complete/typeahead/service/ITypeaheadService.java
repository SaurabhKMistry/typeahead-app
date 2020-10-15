package com.auto.complete.typeahead.service;

import java.util.List;

interface ITypeaheadService {
	List<String> autocomplete(String prefix, int suggestionCount);
}
