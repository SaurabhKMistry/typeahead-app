package com.typeahead.repository;

import java.util.List;

public interface ITypeaheadRepository {
	List<String> autocomplete(String prefix, int suggestionCount);
}
