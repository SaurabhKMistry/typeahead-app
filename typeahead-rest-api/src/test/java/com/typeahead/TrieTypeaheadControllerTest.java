package com.typeahead;

import com.typeahead.controller.TypeaheadController;
import com.typeahead.service.TrieService;
import org.junit.jupiter.api.BeforeAll;

import static org.mockito.Mockito.mock;

public class TrieTypeaheadControllerTest extends TypeaheadTestBase {
	@BeforeAll
	static void beforeAll() {
		mockService = mock(TrieService.class);
		controller = new TypeaheadController(mockService);
	}
}
