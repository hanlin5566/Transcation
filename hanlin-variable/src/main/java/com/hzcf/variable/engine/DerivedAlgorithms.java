package com.hzcf.variable.engine;

import java.util.Map;

/**
 * Create by hanlin on 2017年11月21日
 * 衍生算法接口，所有衍生算法应该实现此类
 **/
public interface DerivedAlgorithms {
	public Object execute(String param) throws Exception;
	public void setVarName(String varName);
	public void setVar(Map<String,Object> var);//数据库中相应变量的信息
}
