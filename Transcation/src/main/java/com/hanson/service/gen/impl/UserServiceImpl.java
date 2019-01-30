package com.hanson.service.gen.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.User;
import com.hanson.dao.gen.mapper.UserMapper;
import com.hanson.service.gen.UserService;

/**
 * Create by hanlin on 2019年1月30日
 **/
public class UserServiceImpl implements UserService{
	@Autowired
	UserMapper mapper;
	
	@Override
	public Integer insert(User user) {
		return mapper.insertSelective(user);
	}
	@Override
	public Integer delete(Integer id) {
		return null;
	}
	@Override
	public Integer update(User user) {
		return null;
	}
	@Override
	public User get(Integer id) {
		return null;
	}
	@Override
	public List<User> search(User user, PageInfo page) {
		return null;
	}
}
