package com.hzcf.variable.engine;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Create by hanlin on 2017年11月21日
 * 衍生变量算法池
 **/
@Component
public class AlgorithmPool {
	//因为都是读，统一在启动时写，没有现成安全问题，所以直接使用hashmap存储接口和衍生变量算法类。 第一个key为接口名字，第二个key为衍生变量名
	private Map<String,Map<String,DerivedAlgorithms>> algorithmMap = new HashMap<String,Map<String,DerivedAlgorithms>>();
	//因为都是读，统一在启动时写，没有现成安全问题，所以直接使用hashmap存储接口和衍生变量信息。 第一个key为接口名字，第二个key为衍生变量名
	private Map<String,Map<String,Object>>  derivedVariableMap = new HashMap<String,Map<String,Object>>();
	
	public void putAlgorithm(String service,String varName,DerivedAlgorithms algorithms){
		if(algorithmMap.containsKey(service)){
			Map<String, DerivedAlgorithms> map = algorithmMap.get(service);
			map.put(varName, algorithms);
		}else{
			Map<String, DerivedAlgorithms> map = new HashMap<String, DerivedAlgorithms>();
			map.put(varName, algorithms);
			algorithmMap.put(service, map);
		}
	}
	
	public Map<String,DerivedAlgorithms> getAlgorithms(String service) {
		return algorithmMap.get(service);
	}

	public void putDerivedVariable(String varName,Map<String,Object> derivedVariable) {
		derivedVariableMap.put(varName,derivedVariable);
	}
	public Map<String,Object> getDerivedVariable(String varName) {
		return derivedVariableMap.get(varName);
	}
}
