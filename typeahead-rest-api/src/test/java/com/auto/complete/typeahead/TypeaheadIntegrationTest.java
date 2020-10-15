package com.auto.complete.typeahead;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TypeaheadIntegrationTest extends TypeaheadTestBase {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Environment env;

	@Nested
	@DisplayName("Happy paths")
	class PositiveTests {
		@Test
		void test_when_suggestion_count_param_is_not_specified_return_def_configured_num_of_suggestions()
		throws Exception {
			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_PREFIX, "alexandar"))
				   .andExpect(status().isOk())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$[0]", is("Alexandar Flemming")));
		}

		@Test
		void test_when_suggestion_count_param_is_specified_return_only_those_num_of_suggestions() throws Exception {
			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_PREFIX, "a")
									.queryParam(QRY_PARAM_SUGGESTION_COUNT, "1"))
				   .andExpect(status().isOk())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$", hasSize(1)));
		}

		@Test
		void test_when_prefix_does_not_match_no_suggestions_are_returned() throws Exception {
			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_PREFIX, "this_is_the_prefix_which_can_not_match_for_sure")
									.queryParam(QRY_PARAM_SUGGESTION_COUNT, "2"))
				   .andExpect(status().isOk())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$", hasSize(0)));
		}

		@Test
		void test_when_a_new_suggestion_is_posted_it_should_be_inserted_into_trie_database() throws Exception {
			mockMvc.perform(post("/typeahead/suggestion")
									.content("{ \"word\" : \"extraordinary\"}")
									.contentType(APPLICATION_JSON))
				   .andExpect(status().isNoContent())
				   .andExpect(content().string(""));
		}

		@Test
		void test_when_a_new_suggestion_is_inserted_into_trie_database_it_should_be_available_for_subsequent_typeahead()
		throws Exception {
			final String rareWord = "a_very_rare_word";
			final String rareWordPrefix = "a_very_rare";

			mockMvc.perform(post("/typeahead/suggestion")
									.content("{ \"word\" : \"" + rareWord + "\"}")
									.contentType(APPLICATION_JSON))
				   .andExpect(status().isNoContent())
				   .andExpect(content().string(""));

			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_PREFIX, rareWordPrefix)
									.queryParam(QRY_PARAM_SUGGESTION_COUNT, "2"))
				   .andExpect(status().isOk())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$", hasSize(1)))
				   .andExpect(jsonPath("$[0]", equalToIgnoringCase(rareWord)));
		}
	}

	@Nested
	@DisplayName("Error scenarios")
	class NegativeTests {
		@Test
		void test_when_prefix_param_is_not_specified_bad_req_err_response_with_apt_msg_is_returned()
		throws Exception {
			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_SUGGESTION_COUNT, "2"))
				   .andExpect(status().isBadRequest())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$.errorCode", is(400)))
				   .andExpect(jsonPath("$.httpCode", is("BAD_REQUEST")))
				   .andExpect(jsonPath("$.errorMessage", is(env.getProperty(PREFIX_QRY_PARAM_MISSING))));
		}

		@Test
		void test_when_suggestion_count_param_is_negative_bad_req_err_response_with_apt_msg_is_returned()
		throws Exception {
			mockMvc.perform(get("/typeahead")
									.queryParam(QRY_PARAM_PREFIX, "a")
									.queryParam(QRY_PARAM_SUGGESTION_COUNT, "-1"))
				   .andExpect(status().isBadRequest())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$.errorCode", is(400)))
				   .andExpect(jsonPath("$.httpCode", is("BAD_REQUEST")))
				   .andExpect(jsonPath("$.errorMessage", is(env.getProperty(INVALID_SUGGESTION_COUNT))));
		}

		@Test
		void test_when_an_empty_req_body_is_sent_in_post_new_suggestion_req_415_errcode_should_be_returned()
		throws Exception {
			mockMvc.perform(post("/typeahead/suggestion"))
				   .andExpect(status().isUnsupportedMediaType());
		}

		@Test
		void test_when_suggestion_word_is_not_specified_in_req_body_bad_req_err_response_with_apt_msg_is_returned()
		throws Exception {
			mockMvc.perform(post("/typeahead/suggestion")
									.content("{ \"some-word\" : \"some-value\"}")
									.contentType(APPLICATION_JSON))
				   .andExpect(status().isBadRequest())
				   .andExpect(content().contentType(APPLICATION_JSON))
				   .andExpect(jsonPath("$.errorCode", is(400)))
				   .andExpect(jsonPath("$.httpCode", is("BAD_REQUEST")))
				   .andExpect(jsonPath("$.errorMessage", is(env.getProperty(MISSING_KEY_IN_REQUEST_BODY))));
		}
	}
}
