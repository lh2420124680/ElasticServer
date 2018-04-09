package com.zjy.helper;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.annotations.ScriptedField;
import org.springframework.data.elasticsearch.core.DefaultEntityMapper;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * 参考源码DefaultResultObjectMapper
 * @see #org.springframework.data.elasticsearch.core.DefaultResultMapper
 * @author 罗浩
 */
public class DefaultResultObjectMapper extends DefaultResultMapper implements ResultsMapper {

	private EntityMapper entityMapper; // 参考DefaultResultMapper的父类AbstractResultMapper
	
	private MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
	
	public DefaultResultObjectMapper() {
		super(new DefaultEntityMapper());
	}

	public DefaultResultObjectMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
		super(new DefaultEntityMapper());
		this.mappingContext = mappingContext;
	}

	public DefaultResultObjectMapper(EntityMapper entityMapper) {
		super(entityMapper);
	}

	public DefaultResultObjectMapper(
			MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
			EntityMapper entityMapper) {
		super(entityMapper);
		this.mappingContext = mappingContext;
	}
	
	/*public DefaultResultObjectMapper(EntityMapper entityMapper) {
		this.entityMapper = entityMapper;
	}*/
	
	@Override
	public EntityMapper getEntityMapper() {
		return this.entityMapper;
	}
	
	public AggregatedPage<Map<String, Object>> mapResults(SearchResponse response, Pageable pageable) {
		long totalHits = response.getHits().totalHits();
		List<Map<String, Object>> results = new ArrayList<>();
		for (SearchHit hit : response.getHits()) {
			if (hit != null) {
				Map<String, Object> result = null;
				if (StringUtils.isNotBlank(hit.sourceAsString())) {
					result = mapEntity(hit.sourceAsString());
				} else {
					result = mapEntity(hit.getFields().values());
				}
				setPersistentEntityId(result, hit.getId());
				populateScriptFields(result, hit);
				results.add(result);
			}
		}

        return new AggregatedPageImpl<Map<String, Object>>(results, pageable, totalHits, response.getAggregations(), response.getScrollId());
	}
	
	private <T> void populateScriptFields(T result, SearchHit hit) {
		if (hit.getFields() != null && !hit.getFields().isEmpty() && result != null) {
			for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
				ScriptedField scriptedField = field.getAnnotation(ScriptedField.class);
				if (scriptedField != null) {
					String name = scriptedField.name().isEmpty() ? field.getName() : scriptedField.name();
					SearchHitField searchHitField = hit.getFields().get(name);
					if (searchHitField != null) {
						field.setAccessible(true);
						try {
							field.set(result, searchHitField.getValue());
						} catch (IllegalArgumentException e) {
							throw new ElasticsearchException("failed to set scripted field: " + name + " with value: "
									+ searchHitField.getValue(), e);
						} catch (IllegalAccessException e) {
							throw new ElasticsearchException("failed to access scripted field: " + name, e);
						}
					}
				}
			}
		}
	}
	
	private <T> void setPersistentEntityId(T result, String id) {

		if (mappingContext != null) {

			ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(Map.class);
			ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

			// Only deal with String because ES generated Ids are strings !
			if (idProperty != null) {
				persistentEntity.getPropertyAccessor(result).setProperty(idProperty, id);
			}

		}
	}
	
	/**
	 * <p>重构父类方法，将json对象转换为map，原父类方法是将实体转为map</p>
	 * @param source
	 * @return
	 */
	public Map<String,Object> mapEntity(String source) {
		if (isBlank(source)) {
			return null;
		}
		try {
			Map<String,Object> mapToObject = JSON.parseObject(source, Map.class);
			return mapToObject;
		} catch (Exception e) {
			throw new ElasticsearchException("failed to map source [ " + source + "] to class map.class", e);
		}
	}
	
	private Map<String,Object> mapEntity(Collection<SearchHitField> values) {
		return mapEntity(buildJSONFromFields(values));
	}
	
	private String buildJSONFromFields(Collection<SearchHitField> values) {
		JsonFactory nodeFactory = new JsonFactory();
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
			generator.writeStartObject();
			for (SearchHitField value : values) {
				if (value.getValues().size() > 1) {
					generator.writeArrayFieldStart(value.getName());
					for (Object val : value.getValues()) {
						generator.writeObject(val);
					}
					generator.writeEndArray();
				} else {
					generator.writeObjectField(value.getName(), value.getValue());
				}
			}
			generator.writeEndObject();
			generator.flush();
			return new String(stream.toByteArray(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			return null;
		}
	}
	
}
