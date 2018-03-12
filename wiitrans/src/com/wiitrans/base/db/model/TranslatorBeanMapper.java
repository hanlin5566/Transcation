package com.wiitrans.base.db.model;

import java.util.List;

// 此为获取译员数据示例
public interface TranslatorBeanMapper {
	// 查询一条数据
	public TranslatorBean Select(int uid);

	// 查询全部数据
	public List<TranslatorBean> SelectAll();

	// 查询内部译员
	public List<TranslatorBean> SelectInternal();

	// 删除一条数据
	public void Delete(int uid);

	// 插入一条数据
	public void Insert(TranslatorBean tran);

	// 更新一条数据
	public void Update(TranslatorBean tran);
	
	// 根据用户ID查询用户信息
	public TranslatorBean SelectUserId(TranslatorBean tran);
	//根据一组用户ID查询一组用户信息
	public List<TranslatorBean> SelectUserIds(List<String> userIds);
}
