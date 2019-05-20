package com.hanson.service;

import java.util.List;

import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.UserAccountRecord;

/**
 * Create by hanlin on 2019年1月28日
 **/
public interface UserAccountRecordService {
	/**
	 * 新增一条数据
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	public Integer insert(UserAccountRecord userAccountRecord);
	/**
	 * 删除一条数据
	 * @param id
	 * @return
	 */
	public Integer delete(Integer id);
	/**
	 * 修改一条数据
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	@Deprecated
	public Integer update(UserAccountRecord userAccountRecord);
	/**
	 * 根据主键获取一条未被删除的数据
	 * @param id
	 * @return
	 */
	public UserAccountRecord get(Integer id);
	/**
	 * 根据条件检索数据
	 * @param {@link User}
	 * @param {@link PageInfo}
	 * @return
	 */
	public List<UserAccountRecord> search(UserAccountRecord userAccountRecord,PageInfo page);
	
	/**
	 * 同步乐观锁充值
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	public Integer rechargeCAS(UserAccountRecord userAccountRecord);
	
	/**
	 * 同步悲观锁充值
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	public Integer rechargePessimism(UserAccountRecord userAccountRecord);
	
	/**
	 * 模拟丢失更新lost modify--异常更新
	 * 两个线程同时更新一条数据，如果其中一个失败，也会导致另一个更新失败。
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	public Integer lostModify_ERROR(UserAccountRecord userAccountRecord);
	/**
	 * 模拟丢失更新lost modify-->正常更新
	 * 两个线程同时更新一条数据，如果其中一个失败，也会导致另一个更新失败。
	 * @param {@link UserAccountRecord} userAccountRecord
	 * @return
	 */
	public Integer lostModify_NORMAL(UserAccountRecord userAccountRecord);
}