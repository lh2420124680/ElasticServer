package com.zjy.biz;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.zjy.helper.ElasticObjectTemplate;
import com.zjy.ibiz.IElasticBiz;
import com.zjy.service.ElasticService;

@Component
public class ElasticBiz implements IElasticBiz {
	
	@Autowired
	private ElasticObjectTemplate elasticObjectTemplate;

	/**
	 * 插入索引库(资源类型)
	 * @return
	 */
	@Override
	public boolean insertIndex(Object obj) {
		Map<String,Object> parseObject = JSON.parseObject(obj.toString(), Map.class);
		boolean result = elasticObjectTemplate.putMapping("index", "tab", parseObject);
		return result;
	}

}
