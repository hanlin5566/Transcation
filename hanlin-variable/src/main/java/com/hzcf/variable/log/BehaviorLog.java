package com.hzcf.variable.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.variable.model.Variable;

/**
 * Create by hanlin on 2017年11月14日
 * 行为日志输出到mongodb 包含  开始时间 接口名称 接口入参 接口回传（数据与状态） 回传时间/用时 规则日志ID 流程日志ID 用户身份证
 **/
public class BehaviorLog{
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
	 * 接口入参
	 */
	private JSONObject param;
	/**
	 * 访问IP
	 */
	private String requestIP;
	/**
	 * 开始时间
	 */
	private Date sTime;
	
	/**
	 * --------------------返回时参数---------------------
	 */
	
	/**
	 * 接口回传
	 */
	private Variable result;
	/**
	 * 回传时间
	 */
	private Date eTime;
	/**
	 * 耗时
	 */
	private long usedTime;
	/**
	 * 返回状态
	 */
	private boolean success;
	/**
	 * 接口算法列表
	 */
	List<AlgorithmLog> algorithmsList = new ArrayList<AlgorithmLog>();
	
	public BehaviorLog(String ruleId, String taskId, String interfaceName, JSONObject param, String requestIP, Date sTime) {
		super();
		this.ruleId = ruleId;
		this.taskId = taskId;
		this.interfaceName = interfaceName;
		this.param = param;
		this.requestIP = requestIP;
		this.sTime = sTime;
	}
	public void addAlgorithm(AlgorithmLog algorithmLog) {
		this.algorithmsList.add(algorithmLog);
	}
	public List<AlgorithmLog> getAlgorithmsList() {
		return algorithmsList;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String roleId) {
		this.ruleId = roleId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getRequestIP() {
		return requestIP;
	}
	public void setRequestIP(String requestIP) {
		this.requestIP = requestIP;
	}
	public Date getsTime() {
		return sTime;
	}
	public void setsTime(Date sTime) {
		this.sTime = sTime;
	}
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public JSONObject getParam() {
		return param;
	}
	public void setParam(JSONObject param) {
		this.param = param;
	}
	public Variable getResult() {
		return result;
	}
	public void setResult(Variable result) {
		this.result = result;
	}
	public Date geteTime() {
		return eTime;
	}
	public void seteTime(Date eTime) {
		this.eTime = eTime;
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
