package com.hzcf.variable.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;


import com.hzcf.variable.engine.DerivedAlgorithms;
import com.hzcf.variable.exception.VariableExecutorException;
import com.hzcf.variable.misc.Constant;

/**
 * Create by hanlin on 2017年11月21日
 * 回传变量值以及执行时间，如果发生异常则向上抛出。
 * TODO:根据传入的className和param调用相应的算法,在listener中全部初始化，在系统中为单利模式。注意线程安全。
 * 多列模式需要传入class在此new出来。并且实例中在listener统一写入的变量需要从本地缓存读取。
 **/
public class VariableServiceCallable implements Callable<Map<String,Object>>{
	private DerivedAlgorithms algorithms;
	private String param;
	private String retVarName;//返回的变量名

	public VariableServiceCallable(DerivedAlgorithms algorithms, String param, String retVarName) {
		super();
		this.algorithms = algorithms;
		this.param = param;
		this.retVarName = retVarName;
	}

	@Override
	public Map<String,Object> call() throws VariableExecutorException {
		long sTime = System.currentTimeMillis();
		HashMap<String, Object> ret = new HashMap<String,Object>();
		ret.put("retVarName", retVarName);
		Object value = "";
		try {
			value = algorithms.execute(this.param);
		} catch (Exception e) {
			throw new VariableExecutorException(retVarName,e);
		}finally{
			ret.put(retVarName, value);
			ret.put(Constant.USED_TIME, String.valueOf(System.currentTimeMillis() - sTime));
		}
		return ret;
	}
}
