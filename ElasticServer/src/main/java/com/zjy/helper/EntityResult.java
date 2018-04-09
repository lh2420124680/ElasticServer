package com.zjy.helper;

import java.io.Serializable;

public class EntityResult<T> implements Serializable {
	private static final long serialVersionUID = 5985823519062101278L;
	private String result = "1";
	private String msg;
	private T rows;

	public EntityResult() {
	}

	public EntityResult(T rows) {
		this.rows = rows;
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMsg() {
		return this.msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getRows() {
		return this.rows;
	}

	public void setRows(T rows) {
		this.rows = rows;
	}
}
