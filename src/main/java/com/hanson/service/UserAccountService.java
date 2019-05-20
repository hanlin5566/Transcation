package com.hanson.service;

import java.util.List;

import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.UserAccount;

/**
 * Create by hanlin on 2019年1月28日
 **/
public interface UserAccountService {
	/**
	 * 新增一条数据
	 * @param {@link UserAccount} userAccount
	 * @return
	 */
	public Integer insert(UserAccount userAccount);
	/**
	 * 删除一条数据
	 * @param id
	 * @return
	 */
	public Integer delete(Integer id);
	/**
	 * 修改一条数据
	 * @param {@link UserAccount} userAccount
	 * @return
	 */
	public Integer update(UserAccount userAccount);
	/**
	 * 根据主键获取一条未被删除的数据
	 * @param id
	 * @return
	 */
	public UserAccount get(Integer id);
	/**
	 * 根据条件检索数据
	 * @param {@link User}
	 * @param {@link PageInfo}
	 * @return
	 */
	public List<UserAccount> search(UserAccount userAccount,PageInfo page);
	
	/**
	 * 查询用户账户
	 * @param {@link User}
	 * @return
	 */
	public UserAccount getUserAccount(Integer userId);
}