package com.wiitrans.base.sync;

import java.util.List;

public class SyncData {
	private String name;
	private String targetURL;
	private String sourceSql;
	private List<String> targetSql;
	
	
	public SyncData(String name, String targetURL, String sourceSql,
			List<String> targetSql) {
		super();
		this.name = name;
		this.targetURL = targetURL;
		this.sourceSql = sourceSql;
		this.targetSql = targetSql;
	}
	public String getTargetURL() {
		return targetURL;
	}
	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSourceSql() {
		return sourceSql;
	}
	public void setSourceSql(String sourceSql) {
		this.sourceSql = sourceSql;
	}
	public List<String> getTargetSql() {
		return targetSql;
	}
	public void setTargetSql(List<String> targetSql) {
		this.targetSql = targetSql;
	}
	
	
}
