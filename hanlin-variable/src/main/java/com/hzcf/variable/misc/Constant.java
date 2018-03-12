package com.hzcf.variable.misc;
/**
 * Create by hanlin on 2017年11月23日
 **/
public class Constant {
	public static final String DIRECT_VARIABLE_ALGORITHMS = "com.hzcf.variable.engine.algorithms.DirectVariableAlgorithms";
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	//用时时长key
	public static final String USED_TIME = "usedTime";
	//暂无衍生变量
	public static final String VAR_NOT_FOUND = "[%s]接口，暂未添加衍生变量。";
	/**
	 * 慢处理提示 taskId,ruleId,service,varName,usedTime 
	 */
	public static final String SLOW_EXECUTOR_INFO = "慢查询日志-taskId:[%s],ruleId:[%s],接口:[%s],衍生变量名称:[%s],处理时间超过%s。";
	/**
	 * 衍生变量错误提示 varDesc,taskId,ruleId,service,varCode 
	 */
	public static final String EXECUTOR_ERROR_INFO = "处理[%s]衍生变量错误-taskId:[%s],ruleId:[%s],接口:[%s],衍生变量标识[%s]。";
	/**
	 * 算法错误提示 taskId,ruleId,service,varName,errorMsg 
	 */
	public static final String ALGORITHM_ERROR_INFO = "算法变量处理错误日志-taskId:[%s],ruleId:[%s],接口:[%s],衍生变量名称:[%s],错误原因:[%s]。";
	/**
	 * 衍生变量行为日志 taskId,ruleId,service,usedTime 
	 */
	public static final String EXECUTOR_RESULT_INFO = "衍生变量处理结果日志-taskId:[%s],ruleId:[%s],接口:[%s],总耗时:[%s]。";
	//
	public static final String DATA_UNKNOWN = "UNKNOWN";
}
