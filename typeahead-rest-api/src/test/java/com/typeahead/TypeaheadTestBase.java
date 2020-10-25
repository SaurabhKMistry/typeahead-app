package com.typeahead;

import com.typeahead.controller.TypeaheadController;
import com.typeahead.service.ESService;
import com.typeahead.service.ITypeaheadService;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.List.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TypeaheadTestBase {
	static final String TYPEAHEAD_SUGGESTION_QBO = "qbo";
	static final String TYPEAHEAD_SUGGESTION_QBDT = "qbdt";
	static final String TYPEAHEAD_PREFIX_Q = "q";
	static final String TYPEAHEAD_PREFIX_QB = "qb";
	static final String QRY_PARAM_PREFIX = "prefix";
	static final String QRY_PARAM_SUGGESTION_COUNT = "suggestionCount";

	static TypeaheadController controller;
	static ITypeaheadService mockService;

	@Test
	public void test_return_num_suggestions_when_suggestion_count_is_more_than_num() {
		List<String> qbList = of(TYPEAHEAD_SUGGESTION_QBDT, TYPEAHEAD_SUGGESTION_QBO);

		int suggestionCount = 10;
		when(mockService.autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount)).thenReturn(qbList);
		List<String> actual = controller.getAutoSuggestions(TYPEAHEAD_PREFIX_Q, suggestionCount);

		verify(mockService).autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount);
		assertThat(actual.size(), is(2));
		assertThat(actual, IsIterableContainingInOrder.contains(TYPEAHEAD_SUGGESTION_QBDT, TYPEAHEAD_SUGGESTION_QBO));
	}

	@Test
	public void test_return_one_suggestion_when_suggestion_count_is_one() {
		int suggestionCount = 1;

		when(mockService.autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount))
				.thenReturn(List.of(TYPEAHEAD_SUGGESTION_QBDT));
		List<String> actual = controller.getAutoSuggestions(TYPEAHEAD_PREFIX_Q, suggestionCount);

		verify(mockService).autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount);
		assertThat(actual.size(), is(suggestionCount));
		assertThat(actual, contains(TYPEAHEAD_SUGGESTION_QBDT));
	}

	@Test
	public void test_return_default_num_suggestion_when_suggestion_count_is_negative() {
		int suggestionCount = -1;

		when(mockService.autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount))
				.thenReturn(List.of(TYPEAHEAD_SUGGESTION_QBDT));
		List<String> actual = controller.getAutoSuggestions(TYPEAHEAD_PREFIX_Q, suggestionCount);

		verify(mockService).autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount);
		assertThat(actual.size(), is(1));
		assertThat(actual, contains(TYPEAHEAD_SUGGESTION_QBDT));
	}

	@Test
	public void test_return_prefix_matching_suggestions() {
		int suggestionCount = 2;
		when(mockService.autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount))
				.thenReturn(List.of(TYPEAHEAD_SUGGESTION_QBDT, TYPEAHEAD_SUGGESTION_QBO));
		List<String> actualSuggestions = controller.getAutoSuggestions(TYPEAHEAD_PREFIX_Q, suggestionCount);

		verify(mockService).autocomplete(TYPEAHEAD_PREFIX_Q, suggestionCount);
		assertThat(actualSuggestions.size(), is(2));
		assertThat(actualSuggestions.get(0), is(TYPEAHEAD_SUGGESTION_QBDT));
		assertThat(actualSuggestions.get(1), is(TYPEAHEAD_SUGGESTION_QBO));
	}
}
