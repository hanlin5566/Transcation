package com.hanson.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanson.base.enums.DataStatus;
import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.UserAccount;
import com.hanson.dao.gen.entity.UserAccountExample;
import com.hanson.dao.gen.mapper.UserAccountMapper;
import com.hanson.service.UserAccountService;
import com.hzcf.base.util.BeanUtils;

/**
 * Create by hanlin on 2019年1月30日
 **/
@Service
public class UserAccountServiceImpl implements UserAccountService{
	@Autowired
	UserAccountMapper mapper;
	
	@Override
	public Integer insert(UserAccount userAccount) {
		return mapper.insertSelective(userAccount);
	}
	@Override
	public Integer delete(Integer id) {
		UserAccount userAccount = new UserAccount();
		userAccount.setId(id);
		userAccount.setDataStatus(DataStatus.DELETED);
		return mapper.updateByPrimaryKeySelective(userAccount);
	}
	@Override
	public Integer update(UserAccount userAccount) {
		return mapper.updateByPrimaryKeySelective(userAccount);
	}
	
	@Override
	public UserAccount get(Integer id) {
		UserAccountExample example = new UserAccountExample();
		example.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL).andIdEqualTo(id);
		List<UserAccount> selectByExample = mapper.selectByExample(example);
		return selectByExample.size() > 0 ? selectByExample.get(0) : null;
	}
	
	@Override
	public List<UserAccount> search(UserAccount userAccount, PageInfo page) {
		UserAccountExample example = BeanUtils.example(userAccount,UserAccountExample.class);
		example.setOrderByClause("id desc");
		List<UserAccount> selectByExample = mapper.selectByExampleWithRowbounds(example, page);
		return selectByExample;
	}
	@Override
	public  UserAccount getUserAccount(Integer userId) {
		UserAccountExample example = new UserAccountExample();
		example.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL).andUserIdEqualTo(userId);
		List<UserAccount> selectByExample = mapper.selectByExample(example);
		return selectByExample.size() > 0 ? selectByExample.get(0) : null;
	}
}