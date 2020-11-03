package com.typeahead.es;

import com.typeahead.es.common.ESConfig;
import com.typeahead.ITypeaheadRepository;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.typeahead.common.TypeaheadConstants.TYPEAHEAD_POWERED_BY_ES;
import static com.typeahead.common.TypeaheadPropertyKeys.TYPEAHEAD_POWERED_BY;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.elasticsearch.client.RestClient.builder;

@Slf4j
@Service(value = "esRepository")
public class ESRepository implements ITypeaheadRepository {
	private RestHighLevelClient esRestClient;

	private ESConfig esConfig;

	@Autowired
	public ESRepository(ESConfig esConfig) {
		this.esConfig = esConfig;
		RestClientBuilder restClientBuilder = builder(
				new HttpHost(esConfig.getHost(), parseInt(esConfig.getPort()), esConfig.getScheme()));
		esRestClient = new RestHighLevelClient(restClientBuilder);
	}

	@SneakyThrows
	public List<String> autocomplete(String prefix, int suggestionCount) {
		CompletionSuggestionBuilder csb = SuggestBuilders.completionSuggestion(esConfig.getESCompletionFieldName())
														 .skipDuplicates(true)
														 .size(suggestionCount);

		SuggestBuilder sb = new SuggestBuilder().setGlobalText(prefix);
		sb.addSuggestion(esConfig.getESCompletionFieldName(), csb);

		SearchRequest req = new SearchRequest(esConfig.getESIndexName());
		req.source().suggest(sb);

		SearchResponse response = esRestClient.search(req, DEFAULT);

		return response.getSuggest()
					   .filter(CompletionSuggestion.class)
					   .stream()
					   .map(CompletionSuggestion::getOptions)
					   .flatMap(Collection::stream)
					   .map(option -> option.getText().string())
					   .collect(toList());
	}
}
