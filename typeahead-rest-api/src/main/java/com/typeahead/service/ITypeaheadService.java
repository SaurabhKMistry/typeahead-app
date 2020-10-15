package com.typeahead.service;

import java.util.List;

public interface ITypeaheadService {
	List<String> autocomplete(String prefix, int suggestionCount);
}
