package com.zjy.entity;

import java.io.Serializable;

import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "indexdb", type = "tab")
public class TestDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String uploadername;
	private String resourcename;

	public String getUploadername() {
		return uploadername;
	}

	public void setUploadername(String uploadername) {
		this.uploadername = uploadername;
	}

	public String getResourcename() {
		return resourcename;
	}

	public void setResourcename(String resourcename) {
		this.resourcename = resourcename;
	}

	@Override
	public String toString() {
		return "TestDto [uploadername=" + uploadername + ", resourcename=" + resourcename + "]";
	}

}
