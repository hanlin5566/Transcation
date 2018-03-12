package com.hzcf.variable.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.variable.engine.AlgorithmPool;
import com.hzcf.variable.engine.DerivedAlgorithms;
import com.hzcf.variable.exception.VariableExecutorException;
import com.hzcf.variable.log.AlgorithmLog;
import com.hzcf.variable.log.BehaviorLog;
import com.hzcf.variable.misc.Constant;
import com.hzcf.variable.model.Receive;
import com.hzcf.variable.model.Variable; 

/**
 * Create by hanlin on 2017年11月21日
 * 根据接口对应的衍生变量数量，生成对应的线程池，跑运算，之后关闭线程池。
 * 记录mongo的日志信息再此封装，包含
 **/
@Service
public class VariableServiceMulitThreadImpl implements VariableService{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	AlgorithmPool pool;//算法池
	
	@Value("${executor.slow.time.threshold}")
	private long slowTimeThreshold;
	
	@Override
	public Variable execute(Receive rec) {
		Date sTime = new Date();
		Variable ret = new Variable();
		boolean success = true;
		Map<String,Object> retValueMap = new HashMap<String,Object>();
		//接口传入的service name
		String service = rec.getService();
		JSONObject param = rec.getParam();
		String param_str = param.toJSONString();
		String taskId = rec.getTaskId();
		String ruleId = rec.getRuleId();
		String requestIP = rec.getRequestIP();
		BehaviorLog behaviorLog = new BehaviorLog(ruleId,taskId,service,param,requestIP,sTime);//接口行为日志。
		//根据接口取得此接口下全部衍生变量
		Map<String,DerivedAlgorithms> algorithms = pool.getAlgorithms(service);
		if(algorithms == null){
			//此接口无衍生变量
			String retInfo = String.format(Constant.VAR_NOT_FOUND, service);
			ret.setMessage(retInfo);
			ret.setSuccess(false);
			ret.setValue(retValueMap);
			Date eTime = new Date();
			long usedTime = eTime.getTime() - sTime.getTime();
			this.setEndingInfo(behaviorLog, ret, eTime,usedTime , success);
			logger.error(retInfo, JSONObject.toJSON(behaviorLog));
		}else{
			//根据衍生变量个数初始化线程池个数
			ExecutorService threadPool = Executors.newFixedThreadPool(algorithms.size());
			CompletionService<Map<String,Object>> cs = new ExecutorCompletionService<Map<String,Object>>(threadPool);
			//提交线程
			for (String varName : algorithms.keySet()) {
				DerivedAlgorithms derivedAlgorithms = algorithms.get(varName);
				VariableServiceCallable variableServiceCallable = new VariableServiceCallable(derivedAlgorithms, param_str,varName);
				cs.submit(variableServiceCallable);
			}
			//获取所有返回值
			for (int i = 0; i<algorithms.keySet().size();i++) {
				//衍生变量缓存信息
				Object value = Constant.DATA_UNKNOWN;
				String varName = Constant.DATA_UNKNOWN;
				AlgorithmLog algorithmLog = new AlgorithmLog(ruleId,taskId,service,varName,Constant.DATA_UNKNOWN,param,requestIP,value,0,true);
				try {
					Map<String,Object> result = cs.take().get();
					//TODO:不全相应线程的信息，有时间改造一下。
					varName = result.get("retVarName").toString();
					Map<String, Object> derivedVariable = pool.getDerivedVariable(varName);
					String className = derivedVariable.get("clazz_name").toString();
					algorithmLog.setVarName(varName);
					algorithmLog.setClassName(className);
					value = result.get(varName).toString();
					String usedTime = result.get(Constant.USED_TIME).toString();
					long usedTime_long = Long.parseLong(usedTime);
					algorithmLog.setUsedTime(usedTime_long);
					algorithmLog.setValue(value);
					if(usedTime_long >= slowTimeThreshold){
						//超出阈值，记录慢查询日志。
						String info = String.format(Constant.SLOW_EXECUTOR_INFO, taskId,ruleId,service,varName,usedTime);
						logger.warn(info,JSONObject.toJSON(algorithmLog));
					}
				} catch (Throwable e) {
					success = false;
					// varDesc,taskId,ruleId,service 
					String info = String.format(Constant.ALGORITHM_ERROR_INFO, taskId,ruleId,service,varName,e.getMessage());
					value = "";
					algorithmLog.setValue(info);
					//异常处理，如果有一个衍生变量出现异常则整体接口处理失败
					if(e.getCause() instanceof VariableExecutorException){
						VariableExecutorException customException = (VariableExecutorException)e.getCause();
						//自定义异常则拿出变量名
						varName = customException.getVarNmae();
						algorithmLog.setVarName(varName);
						algorithmLog.setSuccess(success);
						//构造返回值异常提示
						Map<String, Object> derivedVariable = pool.getDerivedVariable(varName);
						String className = derivedVariable.get("clazz_name").toString();
						algorithmLog.setClassName(className);
						String varDesc = derivedVariable.get("description").toString();
						String retInfo = String.format(Constant.EXECUTOR_ERROR_INFO,varDesc,taskId,ruleId,service,varName);
						ret.setMessage(retInfo);
						//构造记录monggoDB的异常信息
						Throwable realThrowable = customException.getCause();
						JSONObject algorithmLogJson = (JSONObject) JSONObject.toJSON(algorithmLog);
						for (StackTraceElement stackTraceElement : realThrowable.getStackTrace()) {
							if(stackTraceElement.getClassName().indexOf("algorithms") >= 0 ){
								Map<String,Object> exOutPut = new HashMap<String,Object>();
								exOutPut.put("fileName",stackTraceElement.getFileName()); 
								exOutPut.put("lineNumber",stackTraceElement.getLineNumber()); 
								exOutPut.put("methodName",stackTraceElement.getMethodName()); 
								exOutPut.put("className", stackTraceElement.getClassName());
								exOutPut.put("localizedMessage", realThrowable.getMessage());
								algorithmLogJson.put("exception",exOutPut);
								break;
							}
						}
						logger.error(info,algorithmLogJson,e);
					}else{
						//未抛出统一的自定义异常，此处应该是代码有问题，需要记录。
						JSONObject algorithmLogJson = (JSONObject) JSONObject.toJSON(algorithmLog);
						List<Map<String,Object>> errorlist = new ArrayList<Map<String,Object>>();
						//打印全部异常堆栈信息
						for (StackTraceElement stackTraceElement : e.getStackTrace()) {
								Map<String,Object> exOutPut = new HashMap<String,Object>();
								exOutPut.put("fileName",stackTraceElement.getFileName()); 
								exOutPut.put("lineNumber",stackTraceElement.getLineNumber()); 
								exOutPut.put("methodName",stackTraceElement.getMethodName()); 
								exOutPut.put("className", stackTraceElement.getClassName());
								exOutPut.put("localizedMessage", e.getMessage());
								errorlist.add(exOutPut);
						}
						algorithmLogJson.put("errorMsg", "追踪到存在未抛出统一异常的算法，请查看堆栈详情。");
						logger.error(info,algorithmLogJson,e);
					}
				}finally {
					behaviorLog.addAlgorithm(algorithmLog);
					retValueMap.put(varName, value);
				}
			}
			ret.setSuccess(success);
			ret.setValue(retValueMap);
			threadPool.shutdown();
			//接口总日志
			Date eTime = new Date();
			long usedTime = eTime.getTime() - sTime.getTime();
			if(success){
				//设置正确的提示。
				String info = String.format(Constant.EXECUTOR_RESULT_INFO, taskId,ruleId,service,usedTime);
				ret.setMessage(info);
			}
			//设置结束信息
			this.setEndingInfo(behaviorLog, ret, eTime,usedTime , success);
			logger.info(ret.getMessage(),JSONObject.toJSON(behaviorLog));
		}
		return ret;
	}

	private void setEndingInfo(BehaviorLog behaviorLog,Variable result,Date eTime,long usedTime,boolean success){
		behaviorLog.setResult(result);
		behaviorLog.seteTime(eTime);
		behaviorLog.setUsedTime(usedTime);
		behaviorLog.setSuccess(success);
	}
	
}
