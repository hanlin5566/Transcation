package com.hzcf.variable.model;

import java.util.Map;

/**
 * Create by hanlin on 2017年11月21日
 * 返回给规则引擎的model
 **/
public class Variable {
	private boolean success;
	private String message;
	private Map<String,Object> value;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Map<String, Object> getValue() {
		return value;
	}
	public void setValue(Map<String, Object> value) {
		this.value = value;
	}
}
