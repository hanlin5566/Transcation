package com.wiitrans.base.db.model;

public class AutomationTaskBean {
	private int task_id;
	private String job_class;
	private String corn;
	private String param;
	private int status;
	private int type;
	private int deleted;
	private String remaker;
	
	
	public int getTask_id() {
		return task_id;
	}
	public void setTask_id(int task_id) {
		this.task_id = task_id;
	}
	public String getCorn() {
		return corn;
	}
	public void setCorn(String corn) {
		this.corn = corn;
	}
	
	public String getJob_class() {
		return job_class;
	}
	public void setJob_class(String job_class) {
		this.job_class = job_class;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getDeleted() {
		return deleted;
	}
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}
	public String getRemaker() {
		return remaker;
	}
	public void setRemaker(String remaker) {
		this.remaker = remaker;
	}
	
	
}
