package com.zjy.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.zjy.helper.DataConvertHelper;
import com.zjy.helper.ElasticObjectTemplate;
import com.zjy.helper.EscapeHelper;
import com.zjy.helper.ListResult;
import com.zjy.helper.StringUtils;

@RestController
@RequestMapping(value = "/ElasticService")
public class ElasticService {

	@Autowired
	private ElasticObjectTemplate elasticObjectTemplate;
	
	/**
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryResource.ashx", method = RequestMethod.GET)
	public ListResult<Map<String, Object>> queryResource(HttpServletRequest request) {
		Map<String, Object> where = DataConvertHelper.getRequestParams(request);
		String indexName = where.get("indexName").toString();
		String typeName = where.get("typeName").toString();
		
		// 创建搜索条件对象(条件   参数名 + "_jz" 为精准匹配, 参数名 + "_mh" 为模糊匹配)
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		String key = "";
		String value = "";
		String mode = "";
		String reallyParam = "";
		for(Map.Entry<String, Object> map : where.entrySet()) {
			key = map.getKey();
			value = map.getValue().toString();
			mode = key.substring(key.length()-3);
			reallyParam = key.substring(0, key.length()-3);
			if ("_jz".equals(mode)) { // 该条件为精准查询
				boolQuery.must(QueryBuilders.termQuery(reallyParam, value));
			} else if ("_mh".equals(mode)) { //  该条件为模糊查询
				boolQuery.must(QueryBuilders.matchQuery(reallyParam, value));
			}
		}
		
		// 创建排序对象 (0是倒叙,1是正序)
		Object sort = where.get("sort");
		Object sortType = where.get("sortType");
		SortBuilder sortBuilder = null;
		if (!StringUtils.IsEmptyOrNull(sort) && !StringUtils.IsEmptyOrNull(sortType)) {
			if ("0".equals(sortType)) { // 倒叙
				sortBuilder = SortBuilders.fieldSort(sort.toString()).order(SortOrder.DESC);
			} else { // 正序
				sortBuilder = SortBuilders.fieldSort(sort.toString()).order(SortOrder.ASC);
			}
		}
		
		// 分页数据
		int pageIndex = StringUtils.IsEmptyOrNull(where.get("pageIndex")) ? 1 : Integer.valueOf(where.get("pageIndex").toString());
		int pageSize = StringUtils.IsEmptyOrNull(where.get("pageSize")) ? 10 : Integer.valueOf(where.get("pageSize").toString());
		
		// 创建SearchQuery查询对象 判断排序对象是否空，是空就不创建排序对象
		SearchQuery searchQuery = null;
		if (sortBuilder == null) {
			searchQuery = new NativeSearchQueryBuilder()
					.withQuery(boolQuery)
					.withPageable(PageRequest.of((pageIndex-1)*pageSize, pageSize))
					.build();
		} else {
			searchQuery = new NativeSearchQueryBuilder()
					.withQuery(boolQuery)
					.withSort(sortBuilder)
					.withPageable(PageRequest.of(pageIndex, pageSize))
					.build();
		}
		List<Map<String, Object>> list = elasticObjectTemplate.queryForList(searchQuery, indexName, typeName);
		
		searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQuery)
				.withIndices(indexName)
				.withTypes(typeName)
				.build();
		long count = elasticObjectTemplate.count(searchQuery);
		
		ListResult<Map<String, Object>> result = new ListResult<Map<String, Object>>();
		result.setRows(list);
		result.setTotal((int)count);
		result.setPageindex(pageIndex);
		result.setPages(pageSize);
		return result;
	}
	
	@RequestMapping(value = "/delete.ashx", method = RequestMethod.POST)
	public boolean insert(HttpServletRequest request) {
		Map<String, Object> where = DataConvertHelper.getRequestParams(request);
		String indexName = where.get("indexName").toString();
		String typeName = where.get("typeName").toString();
		String gid = where.get("gid").toString();
		elasticObjectTemplate.delete(indexName, typeName, gid);
		return false;
	}
	
}
