package com.hanson.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanson.base.enums.DataStatus;
import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.User;
import com.hanson.dao.gen.entity.UserExample;
import com.hanson.dao.gen.mapper.UserMapper;
import com.hanson.service.UserService;
import com.hanson.base.util.BeanUtils;

/**
 * Create by hanlin on 2019年1月30日
 **/
@Service
public class UserServiceImpl implements UserService{
	@Autowired
	UserMapper mapper;
	
	@Override
	public Integer insert(User user) {
		return mapper.insertSelective(user);
	}
	@Override
	public Integer delete(Integer id) {
		User user = new User();
		user.setId(id);
		user.setDataStatus(DataStatus.DELETED);
		return mapper.updateByPrimaryKeySelective(user);
	}
	@Override
	public Integer update(User user) {
		return mapper.updateByPrimaryKeySelective(user);
	}
	
	@Override
	public User get(Integer id) {
		UserExample example = new UserExample();
		example.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL).andIdEqualTo(id);
		List<User> selectByExample = mapper.selectByExample(example);
		return selectByExample.size() > 0 ? selectByExample.get(0) : null;
	}
	
	@Override
	public List<User> search(User user, PageInfo page) {
		UserExample example = BeanUtils.example(user,UserExample.class);
		example.setOrderByClause("id desc");
		List<User> selectByExample = mapper.selectByExampleWithRowbounds(example, page);
		return selectByExample;
	}
}