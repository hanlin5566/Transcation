package com.wiitrans.base.cache;

public interface ICache {

	// 初始化缓存
	int Init(String url);

	// 清理缓存
	int UnInit();

	// 设置字符串对象到缓存
	int SetString(int node_id, String key, String val);

	// 获取字符串对象到缓存
	String GetString(int node_id, String key);

	// 从缓存中删除字符串对象
	int DelString(int node_id, String key);
}
