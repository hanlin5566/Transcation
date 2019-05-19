package com.hanson.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanson.base.enums.DataStatus;
import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.base.response.AccountResponseCode;
import com.hanson.dao.gen.entity.UserAccount;
import com.hanson.dao.gen.entity.UserAccountExample;
import com.hanson.dao.gen.entity.UserAccountRecord;
import com.hanson.dao.gen.entity.UserAccountRecordExample;
import com.hanson.dao.gen.mapper.UserAccountMapper;
import com.hanson.dao.gen.mapper.UserAccountRecordMapper;
import com.hanson.dao.gen.mapper.ext.UserAccountRecordExtMapper;
import com.hanson.service.UserAccountRecordService;
import com.hzcf.base.exception.ServiceException;
import com.hzcf.base.util.BeanUtils;

/**
 * Create by hanlin on 2019年1月30日
 **/
@Service
public class UserAccountRecordServiceImpl implements UserAccountRecordService {
	@Autowired
	UserAccountRecordMapper mapper;
	@Autowired
	UserAccountRecordExtMapper mapper_ext;
	@Autowired
	UserAccountMapper userAccountmapper;

	@Override
	public Integer insert(UserAccountRecord userAccountRecord) {
		return mapper.insertSelective(userAccountRecord);
	}

	@Override
	public Integer delete(Integer id) {
		UserAccountRecord userAccountRecord = new UserAccountRecord();
		userAccountRecord.setId(id);
		userAccountRecord.setDataStatus(DataStatus.DELETED);
		return mapper.updateByPrimaryKeySelective(userAccountRecord);
	}

	@Override
	public UserAccountRecord get(Integer id) {
		UserAccountRecordExample example = new UserAccountRecordExample();
		example.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL).andIdEqualTo(id);
		List<UserAccountRecord> selectByExample = mapper.selectByExample(example);
		return selectByExample.size() > 0 ? selectByExample.get(0) : null;
	}

	@Override
	public List<UserAccountRecord> search(UserAccountRecord userAccountRecord, PageInfo page) {
		UserAccountRecordExample example = BeanUtils.example(userAccountRecord, UserAccountRecordExample.class);
		example.setOrderByClause("id desc");
		List<UserAccountRecord> selectByExample = mapper.selectByExampleWithRowbounds(example, page);
		return selectByExample;
	}

	@Override
	public Integer update(UserAccountRecord userAccountRecord) {
		return null;
	}

	/**
	 * 通过判断version 乐观锁，充值，并未自旋失败之后未重试。
	 */
	@Transactional
	@Override
	public Integer rechargeCAS(UserAccountRecord userAccountRecord) {
		// 根据userId 获取用户账户信息
		UserAccountExample userAccountExample = new UserAccountExample();
		userAccountExample.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL)
				.andUserIdEqualTo(userAccountRecord.getUserId());
		List<UserAccount> userAccountList = userAccountmapper.selectByExample(userAccountExample);
		// 账户未生成
		if (userAccountList == null || userAccountList.size() <= 0) {
			// throw new
			// ServiceException(AccountResponseCode.ACCOUNT_NOT_FOUND,String.format(AccountResponseCode.ACCOUNT__NOT_FOUND.detailMsg(),
			// userAccountRecord.getUserId()));
			throw new ServiceException(AccountResponseCode.ACCOUNT_NOT_FOUND);
		}
		// 获取账户余额
		UserAccount userAccount = userAccountList.get(0);
		Long balance = userAccount.getBalance();
		// 增加余额
		balance += userAccountRecord.getMoney();
		// 更新余额
		userAccount.setBalance(balance);
		// 设置更新人
		userAccount.setUpdateUid(userAccountRecord.getUpdateUid());
		int updateByCAS = mapper_ext.updateByCAS(userAccount);
		if (updateByCAS <= 0) {
			// 更新失败触发异常
			throw new ServiceException(AccountResponseCode.ACCOUNT_RECHARGE_OPTIMISTICED_LOCKED, String.format(
					AccountResponseCode.ACCOUNT_RECHARGE_OPTIMISTICED_LOCKED.detailMsg(), userAccount.getVersion()));
		}
		// 记录流水
		userAccountRecord.setBalance(balance);
		userAccountRecord.setVersion(userAccount.getVersion());
		userAccountRecord.setDataStatus(DataStatus.NORMAL);
		int insertSelective = mapper.insertSelective(userAccountRecord);
		return insertSelective;
	}
	
	@Transactional
	@Override
	public Integer rechargePessimism(UserAccountRecord userAccountRecord) {
		// 根据userId 获取用户账户信息
		UserAccountExample userAccountExample = new UserAccountExample();
		userAccountExample.createCriteria().andDataStatusEqualTo(DataStatus.NORMAL)
				.andUserIdEqualTo(userAccountRecord.getUserId());
		//悲观锁，锁住此行记录
		List<UserAccount> userAccountList = mapper_ext.selectByExampleForUpdate(userAccountExample);
		//账户未生成
		if (userAccountList == null || userAccountList.size() <= 0) {
			// throw new
			// ServiceException(AccountResponseCode.ACCOUNT_NOT_FOUND,String.format(AccountResponseCode.ACCOUNT__NOT_FOUND.detailMsg(),
			// userAccountRecord.getUserId()));
			throw new ServiceException(AccountResponseCode.ACCOUNT_NOT_FOUND);
		}
		// 获取账户余额
		UserAccount userAccount = userAccountList.get(0);
		Long balance = userAccount.getBalance();
		// 增加余额
		balance += userAccountRecord.getMoney();
		// 更新余额
		userAccount.setBalance(balance);
		//增加版本信息
		Byte version = userAccount.getVersion();
		version++;
		userAccount.setVersion(version);
		// 设置更新人
		userAccount.setUpdateUid(userAccountRecord.getUpdateUid());
		int updateByPessimism = userAccountmapper.updateByPrimaryKeySelective(userAccount);
		if (updateByPessimism <= 0) {
			// 更新失败触发异常
			throw new ServiceException(AccountResponseCode.ACCOUNT_RECHARGE_OPTIMISTICED_LOCKED, String.format(
					AccountResponseCode.ACCOUNT_RECHARGE_OPTIMISTICED_LOCKED.detailMsg(), userAccount.getVersion()));
		}
		// 记录流水
		userAccountRecord.setBalance(balance);
		userAccountRecord.setVersion(userAccount.getVersion());
		int insertSelective = mapper.insertSelective(userAccountRecord);
		return insertSelective;
	}

	@Override
	public Integer lostModify_ERROR(UserAccountRecord userAccountRecord) {
		return null;
	}

	@Override
	public Integer lostModify_NORMAL(UserAccountRecord userAccountRecord) {
		return null;
	}
}