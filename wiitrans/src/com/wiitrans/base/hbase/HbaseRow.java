package com.wiitrans.base.hbase;

import java.util.Map;


public class HbaseRow {
	private String colFamily;
	private String rowKey;
	private Map<String, String> cols;
	
	public String getColFamily() {
		return colFamily;
	}
	public void setColFamily(String colFamily) {
		this.colFamily = colFamily;
	}
	public String getRowKey() {
		return rowKey;
	}
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}
	public Map<String, String> getCols() {
		return cols;
	}
	public void setCols(Map<String, String> cols) {
		this.cols = cols;
	}
	
	public HbaseRow(String colFamily, String rowKey, Map<String, String> cols) {
		super();
		this.colFamily = colFamily;
		this.rowKey = rowKey;
		this.cols = cols;
	}
}
