package com.typeahead;

import java.util.Collection;

public interface ITypeaheadRepository {
	Collection<String> autocomplete(String prefix, int suggestionCount);
}
