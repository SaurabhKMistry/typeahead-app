package com.typeahead;

import com.typeahead.controller.TypeaheadController;
import com.typeahead.es.ESService;
import org.junit.jupiter.api.BeforeAll;

import static org.mockito.Mockito.mock;

class ESTypeaheadControllerTest extends TypeaheadTestBase {
	@BeforeAll
	static void beforeAll() {
		mockService = mock(ESService.class);
		controller = new TypeaheadController(mockService);
	}
}