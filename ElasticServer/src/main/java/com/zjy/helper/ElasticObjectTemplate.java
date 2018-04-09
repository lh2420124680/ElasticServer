package com.zjy.helper;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.elasticsearch.index.VersionType.EXTERNAL;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.IndexBoost;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.ScriptField;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;


/**
 * 参考源码ElasticsearchTemplate
 * @see #org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
 * @author 罗浩
 */
public class ElasticObjectTemplate extends ElasticsearchTemplate {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticObjectTemplate.class);
	private Client client;
	private DefaultResultObjectMapper resultsMapper;
	private ElasticsearchConverter elasticsearchConverter;
	private String searchTimeout;
	
	@Override
	public Client getClient() {
		return client;
	}
	
	@Override
	public ElasticsearchConverter getElasticsearchConverter() {
		return elasticsearchConverter;
	}

	public void setSearchTimeout(String searchTimeout) {
		this.searchTimeout = searchTimeout;
	}
	
	protected DefaultResultObjectMapper getResultsMapper() {
		return resultsMapper;
	}
	
	// >>>>>构造<<<<<<<
	public ElasticObjectTemplate(Client client) {
		this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()));
	}

	public ElasticObjectTemplate(Client client, EntityMapper entityMapper) {
		this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()), entityMapper);
	}

	public ElasticObjectTemplate(Client client, ElasticsearchConverter elasticsearchConverter,
			EntityMapper entityMapper) {
		this(client, elasticsearchConverter,
				new DefaultResultObjectMapper(elasticsearchConverter.getMappingContext(), entityMapper));
	}

	public ElasticObjectTemplate(Client client, DefaultResultObjectMapper resultsMapper) {
		this(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()), resultsMapper);
	}

	public ElasticObjectTemplate(Client client, ElasticsearchConverter elasticsearchConverter) {
		this(client, elasticsearchConverter, new DefaultResultObjectMapper(elasticsearchConverter.getMappingContext()));
	}

	public ElasticObjectTemplate(Client client, ElasticsearchConverter elasticsearchConverter,
			DefaultResultObjectMapper resultsMapper) {
		super(client,elasticsearchConverter,resultsMapper);
		Assert.notNull(client, "Client must not be null!");
		Assert.notNull(elasticsearchConverter, "ElasticsearchConverter must not be null!");
		Assert.notNull(resultsMapper, "ResultsMapper must not be null!");

		this.client = client;
		this.elasticsearchConverter = elasticsearchConverter;
		this.resultsMapper = resultsMapper;
	}
	 
	//>>>>>>>>>>>>>>>>>>>queryForList<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	/**
	 * <p>重构父类的queryForList方法，由于父类会对实体进行注解检测，所以这边改成万能的Map,使用起来更加简单</p>
	 * @param query 查询条件对象(包含查询条件，排序，分页等)
	 * @param indexName 需要查询的索引库的名称(相当于数据库名称)
	 * @param typeName 需要查询的索引库中类型的名称(相当于数据库表的名称)
	 * @return
	 */
	public List<Map<String, Object>> queryForList(SearchQuery query, String indexName, String typeName) {
		return queryForPage(query, indexName, typeName).getContent();
	}
	
	public AggregatedPage<Map<String, Object>> queryForPage(SearchQuery query, String indexName, String typeName) {
		return queryForPage(query, indexName, typeName, resultsMapper);
	}
	
	public AggregatedPage<Map<String, Object>> queryForPage(SearchQuery query, String indexName, String typeName, DefaultResultObjectMapper mapper) {
		SearchResponse response = doSearch(prepareSearch(query, indexName, typeName), query);
		return mapper.mapResults(response, query.getPageable());
	}
	
	private SearchResponse doSearch(SearchRequestBuilder searchRequest, SearchQuery searchQuery) {
		if (searchQuery.getFilter() != null) {
			searchRequest.setPostFilter(searchQuery.getFilter());
		}

		if (!isEmpty(searchQuery.getElasticsearchSorts())) {
			for (SortBuilder sort : searchQuery.getElasticsearchSorts()) {
				searchRequest.addSort(sort);
			}
		}

		if (!searchQuery.getScriptFields().isEmpty()) {
			//_source should be return all the time
			//searchRequest.addStoredField("_source");
			for (ScriptField scriptedField : searchQuery.getScriptFields()) {
				searchRequest.addScriptField(scriptedField.fieldName(), scriptedField.script());
			}
		}

		if (searchQuery.getHighlightFields() != null) {
			for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
				searchRequest.highlighter(new HighlightBuilder().field(highlightField));
			}
		}

		if (!isEmpty(searchQuery.getIndicesBoost())) {
			for (IndexBoost indexBoost : searchQuery.getIndicesBoost()) {
				searchRequest.addIndexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
			}
		}

		if (!isEmpty(searchQuery.getAggregations())) {
			for (AbstractAggregationBuilder aggregationBuilder : searchQuery.getAggregations()) {
				searchRequest.addAggregation(aggregationBuilder);
			}
		}

		if (!isEmpty(searchQuery.getFacets())) {
			for (FacetRequest aggregatedFacet : searchQuery.getFacets()) {
				searchRequest.addAggregation(aggregatedFacet.getFacet());
			}
		}
		return getSearchResponse(searchRequest.setQuery(searchQuery.getQuery()).execute());
	}
	
	private SearchResponse getSearchResponse(ListenableActionFuture<SearchResponse> response) {
		return searchTimeout == null ? response.actionGet() : response.actionGet(searchTimeout);
	}
	
	private <T> SearchRequestBuilder prepareSearch(Query query, String indexName, String typeName) {
		setPersistentEntityIndexAndType(query, indexName, typeName);
		return prepareSearch(query);
	}

	private SearchRequestBuilder prepareSearch(Query query) {
		Assert.notNull(query.getIndices(), "No index defined for Query");
		Assert.notNull(query.getTypes(), "No type defined for Query");

		int startRecord = 0;
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(toArray(query.getIndices()))
				.setSearchType(query.getSearchType()).setTypes(toArray(query.getTypes()));

		if (query.getSourceFilter() != null) {
			SourceFilter sourceFilter = query.getSourceFilter();
			searchRequestBuilder.setFetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
		}

		if (query.getPageable().isPaged()) {
			startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
			searchRequestBuilder.setSize(query.getPageable().getPageSize());
		}
		searchRequestBuilder.setFrom(startRecord);

		if (!query.getFields().isEmpty()) {
			searchRequestBuilder.setFetchSource(toArray(query.getFields()),null);
		}

		if (query.getSort() != null) {
			for (Sort.Order order : query.getSort()) {
				searchRequestBuilder.addSort(order.getProperty(),
						order.getDirection() == Sort.Direction.DESC ? SortOrder.DESC : SortOrder.ASC);
			}
		}

		if (query.getMinScore() > 0) {
			searchRequestBuilder.setMinScore(query.getMinScore());
		}
		return searchRequestBuilder;
	}
	
	private static String[] toArray(List<String> values) {
		String[] valuesAsArray = new String[values.size()];
		return values.toArray(valuesAsArray);
	}
	
	private void setPersistentEntityIndexAndType(Query query, String indexName, String typeName) {
		if (query.getIndices().isEmpty()) {
			query.addIndices(retrieveIndexNameFromPersistentEntity(indexName));
		}
		if (query.getTypes().isEmpty()) {
			query.addTypes(retrieveTypeFromPersistentEntity(typeName));
		}
	}

	private String[] retrieveIndexNameFromPersistentEntity(String indexName) {
		if (indexName != null) {
			String[] split = indexName.split(",");
			return split;
		}
		return null;
	}

	private String[] retrieveTypeFromPersistentEntity(String typeName) {
		if (typeName != null) {
			String[] split = typeName.split(",");
			return split;
		}
		return null;
	}
	
	/**
	 * 单个插入的方法
	 */
	@Override
	public String index(IndexQuery query) {
		String documentId = prepareIndex(query).execute().actionGet().getId();
		// 此方法需要注释 不然会去检验实体的@Decoumnet注解
		// We should call this because we are not going through a mapper.
		/*if (query.getObject() != null) {
			setPersistentEntityId(query.getObject(), documentId);
		}*/
		return documentId;
	}
	
	private IndexRequestBuilder prepareIndex(IndexQuery query) {
		try {
			String indexName = query.getIndexName();
			String type = query.getType();

			IndexRequestBuilder indexRequestBuilder = null;

			if (query.getObject() != null) {
				String id = isBlank(query.getId()) ? getPersistentEntityId(query.getObject()) : query.getId();
				// If we have a query id and a document id, do not ask ES to generate one.
				if (id != null) {
					indexRequestBuilder = client.prepareIndex(indexName, type, id);
				} else {
					indexRequestBuilder = client.prepareIndex(indexName, type);
				}
				indexRequestBuilder.setSource(jsonStrToArr(query.getObject()));
			} else if (query.getSource() != null) {
				indexRequestBuilder = client.prepareIndex(indexName, type, query.getId()).setSource(query.getSource());
			} else {
				throw new ElasticsearchException(
						"object or source is null, failed to index the document [id: " + query.getId() + "]");
			}
			if (query.getVersion() != null) {
				indexRequestBuilder.setVersion(query.getVersion());
				indexRequestBuilder.setVersionType(EXTERNAL);
			}

			if (query.getParentId() != null) {
				indexRequestBuilder.setParent(query.getParentId());
			}

			return indexRequestBuilder;
		} catch (Exception e) {
			throw new ElasticsearchException("failed to index the document [id: " + query.getId() + "]", e);
		}
	}
	
	private void setPersistentEntityId(Object entity, String id) {

		ElasticsearchPersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
		ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

		// Only deal with text because ES generated Ids are strings !

		if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
			persistentEntity.getPropertyAccessor(entity).setProperty(idProperty, id);
		}
	}
	
	private String getPersistentEntityId(Object entity) {

		ElasticsearchPersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
		Object identifier = persistentEntity.getIdentifierAccessor(entity).getIdentifier();

		if (identifier != null){
			return identifier.toString();
		}

		return null;
	}
	
	/**
	 * json字符串转数组
	 * @param jsonStr
	 * @return
	 * @see #org.elasticsearch.action.index.IndexRequest.source
	 */
	private Object[] jsonStrToArr(Object jsonStr) {
		Map<Object,String> parseObject = JSON.parseObject(jsonStr.toString(), Map.class);
		Object[] arr = new Object[parseObject.size() * 2];
		int i = 0;
		for (Map.Entry<Object, String> entry : parseObject.entrySet()) {
			Object key = entry.getKey();
			String value = entry.getValue();
			arr[i] = key;
			arr[i+1] = value;
			i = i + 2;
		}
		return arr;
	}

	
}
