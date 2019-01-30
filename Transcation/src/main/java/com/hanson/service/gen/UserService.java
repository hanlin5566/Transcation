package com.hanson.service.gen;

import java.util.List;

import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.User;

/**
 * Create by hanlin on 2019年1月28日
 **/
public interface UserService {
	/**
	 * 新增一条数据
	 * @param {@link User} user
	 * @return
	 */
	public Integer insert(User user);
	/**
	 * 删除一条数据
	 * @param id
	 * @return
	 */
	public Integer delete(Integer id);
	/**
	 * 修改一条数据
	 * @param {@link User} user
	 * @return
	 */
	public Integer update(User user);
	/**
	 * 根据主键获取一条未被删除的数据
	 * @param id
	 * @return
	 */
	public User get(Integer id);
	/**
	 * 根据条件检索数据
	 * @param {@link User}
	 * @param {@link PageInfo}
	 * @return
	 */
	public List<User> search(User user,PageInfo page);
}