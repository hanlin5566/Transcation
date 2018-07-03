/**
 * 
 */
package com.hzcf.edge.common.utils;


import org.apache.log4j.Logger;

public abstract class Logs {


	public static final Logger query = Logger
			.getLogger("query");

	public static final Logger appData = Logger
			.getLogger("appData");

	public static final Logger sendMsg = Logger
			.getLogger("sendMsg");

	public static final Logger insert = Logger
			.getLogger("insert");

	public static final Logger status_302 = Logger
			.getLogger("status_302");

	public static final Logger threadStatus = Logger
			.getLogger("threadStatus");

	public static final Logger redisCache = Logger
			.getLogger("redisCache");

	/** 用于统计任务执行结果，加日志 */
	public enum StatsResult {
		
		SUCESS, /** 任务成功 */ 
		FAIL, /** 任务失败 */ 
		PARTIAL, /** 任务部分成功 */ 
		ASYNC /** 任务已提交异步处理 */
	}
}
