package com.typeahead.repository;

import java.util.Collection;
import java.util.List;

public interface ITypeaheadRepository {
	Collection<String> autocomplete(String prefix, int suggestionCount);
}
