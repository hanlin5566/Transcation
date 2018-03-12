package com.hzcf.variable.log;

import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月23日
 * 算法日志
 **/
public class AlgorithmLog{
	/**
	 * 规则ID，用于串联流程日志
	 */
	private String ruleId;
	/**
	 * 任务ID，用于串联流程日志
	 */
	private String taskId;
	/**
	 * 接口名称
	 */
	private String interfaceName;
	/**
	 * 变量名称
	 */
	private String varName;
	/**
	 * 类名称
	 */
	private String className;
	
	/**
	 * 接口入参
	 */
	private JSONObject param;
	/**
	 * 访问IP
	 */
	private String requestIP;
	/**
	 * --------------------返回时参数---------------------
	 */
	
	/**
	 * 接口回传
	 */
	private Object value;
	/**
	 * 耗时
	 */
	private long usedTime;
	/**
	 * 返回状态
	 */
	private boolean success;
	public AlgorithmLog(String ruleId, String taskId, String interfaceName, String varName, String className,
			JSONObject param, String requestIP, Object value, long usedTime, boolean success) {
		super();
		this.ruleId = ruleId;
		this.taskId = taskId;
		this.interfaceName = interfaceName;
		this.varName = varName;
		this.className = className;
		this.param = param;
		this.requestIP = requestIP;
		this.value = value;
		this.usedTime = usedTime;
		this.success = success;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getVarName() {
		return varName;
	}
	public void setVarName(String varName) {
		this.varName = varName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public JSONObject getParam() {
		return param;
	}
	public void setParam(JSONObject param) {
		this.param = param;
	}
	public String getRequestIP() {
		return requestIP;
	}
	public void setRequestIP(String requestIP) {
		this.requestIP = requestIP;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public long getUsedTime() {
		return usedTime;
	}
	public void setUsedTime(long usedTime) {
		this.usedTime = usedTime;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
}
