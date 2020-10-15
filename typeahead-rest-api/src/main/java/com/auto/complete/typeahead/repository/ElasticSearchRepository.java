package com.auto.complete.typeahead.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.auto.complete.typeahead.TypeaheadPropertyKeys.*;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.client.RestClient.builder;

@Repository
@Slf4j
public class ElasticSearchRepository {
	private RestHighLevelClient client;

	private String esIndexName;
	private String esFieldName;

	@Autowired
	public ElasticSearchRepository(Environment env) {
		String host = env.getProperty(ES_HOST, DEFAULT_HOST);
		String port = env.getProperty(ES_PORT, DEFAULT_PORT);
		String scheme = env.getProperty(ES_SCHEME, DEFAULT_SCHEME);

		esIndexName = env.getProperty(ES_INDEX, DEFAULT_ES_INDEX);
		esFieldName = env.getProperty(ES_FIELD_NAME, DEFAULT_ES_FIELD);

		RestClientBuilder restClientBuilder = builder(new HttpHost(host, parseInt(port), scheme));
		client = new RestHighLevelClient(restClientBuilder);
	}

	@SneakyThrows
	public List<String> autocomplete(String prefix, int suggestionCount) {
		SuggestBuilder sb = new SuggestBuilder().setGlobalText(prefix);
		CompletionSuggestionBuilder csb = SuggestBuilders.completionSuggestion(esFieldName)
														 .skipDuplicates(true)
														 .size(suggestionCount);
		sb.addSuggestion(esFieldName, csb);

		SearchRequest req = new SearchRequest(esIndexName);
		req.source().suggest(sb);

		SearchResponse res = client.search(req, DEFAULT);

		return res.getSuggest()
				  .filter(CompletionSuggestion.class)
				  .stream()
				  .map(CompletionSuggestion::getOptions)
				  .flatMap(Collection::stream)
				  .map(option -> option.getText().string())
				  .distinct()
				  .collect(toList());
	}
}
