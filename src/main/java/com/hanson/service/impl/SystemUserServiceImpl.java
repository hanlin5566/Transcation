package com.hanson.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanson.base.enums.DataStatus;
import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.SystemUser;
import com.hanson.dao.gen.entity.SystemUserExample;
import com.hanson.dao.gen.mapper.SystemUserMapper;
import com.hanson.service.SystemUserService;
import com.hzcf.base.util.BeanUtils;

/**
 * Create by hanlin on 2019年1月28日
 **/
@Service
public class SystemUserServiceImpl implements SystemUserService {
	@Autowired
	SystemUserMapper systemUserMapper;

	@Override
	public SystemUser get(Integer id) {
		SystemUserExample example = new SystemUserExample();
		example.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL).andIdEqualTo(id);
		List<SystemUser> selectByExample = systemUserMapper.selectByExample(example);
		return selectByExample.size() > 0 ? selectByExample.get(0) : null;
	}

	@Override
	public List<SystemUser> search(SystemUser systemUser,PageInfo page) {
		SystemUserExample example = BeanUtils.example(systemUser,SystemUserExample.class);
		example.setOrderByClause("id desc");
		List<SystemUser> selectByExample = systemUserMapper.selectByExampleWithRowbounds(example, page);
		return selectByExample;
	}

	@Override
	public Integer delete(Integer id) {
		SystemUser systemUser = new SystemUser();
		systemUser.setId(id);
		systemUser.setDataStatus(DataStatus.DELETED);
		return systemUserMapper.updateByPrimaryKeySelective(systemUser);
	}

	@Override
	public Integer update(SystemUser systemUser) {
		return systemUserMapper.updateByPrimaryKeySelective(systemUser);
	}

	@Override
	public Integer insert(SystemUser systemUser) {
		return systemUserMapper.insertSelective(systemUser);
	}
	

	@Override
	@Transactional
	public Integer addAccount(SystemUser systemUser) {
		systemUserMapper.updateByPrimaryKeySelective(systemUser);
		int i = 10/0;
		return i;
	}
}
