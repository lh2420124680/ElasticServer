package com.zjy.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjy.biz.ElasticBiz;
import com.zjy.helper.ElasticObjectTemplate;
import com.zjy.helper.EscapeHelper;
import com.zjy.helper.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ElasticReceiver implements MessageListener {

	private ElasticObjectTemplate elasticObjectTemplate;

	public ElasticObjectTemplate getElasticObjectTemplate() {
		return elasticObjectTemplate;
	}

	public void setElasticObjectTemplate(ElasticObjectTemplate elasticObjectTemplate) {
		this.elasticObjectTemplate = elasticObjectTemplate;
	}

	public void onMessage(Message message) {
		try {
			String msg = ((TextMessage) message).getText();
			String data = java.net.URLDecoder.decode(msg, "UTF-8");
			
			String unescape = EscapeHelper.unescape(data);
			
			JSONObject parseObject = JSON.parseObject(unescape);
			String dataId = StringUtils.GetGUID();
			String indexName = parseObject.getString("indexName");
			String typeName = parseObject.getString("typeName");
			String entity = parseObject.getString("entity");
			
			IndexQuery indexQuery = new IndexQueryBuilder().withId(dataId).withIndexName(indexName).withType(typeName).withObject(entity).build();  
			String index = elasticObjectTemplate.index(indexQuery);
			System.out.println("indexName："+indexName + index);
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
