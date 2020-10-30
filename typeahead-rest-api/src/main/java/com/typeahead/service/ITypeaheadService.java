package com.typeahead.service;

import java.util.Collection;

public interface ITypeaheadService {
	Collection<String> autocomplete(String prefix, int suggestionCount);
}
