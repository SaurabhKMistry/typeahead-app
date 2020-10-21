package com.typeahead;

import com.typeahead.controller.TypeaheadController;
import com.typeahead.service.ESService;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.List.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.*;

class TypeaheadControllerTest extends TypeaheadTestBase {
	private static TypeaheadController controller;
	private static ESService mockService;

	@BeforeAll
	private static void beforeAll() {
		mockService = mock(ESService.class);
		controller = new TypeaheadController(mockService);
	}

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
}