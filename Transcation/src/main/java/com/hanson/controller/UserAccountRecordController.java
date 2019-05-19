package com.hanson.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanson.base.enums.AccountBusinessType;
import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.UserAccountRecord;
import com.hanson.service.UserAccountRecordService;
import com.hanson.service.UserAccountService;
import com.hzcf.base.exception.ControllerException;
//TODO:需要换成自己的responseCode
import com.hzcf.base.response.ResponseCode;
import com.hzcf.base.response.ResponseData;

/**
 * Create by hanlin on 2019年1月28日
 **/
@RestController()
@RequestMapping(value = "/api/account")
public class UserAccountRecordController {
	@Autowired
	UserAccountRecordService userAccountRecordService;
	@Autowired
	UserAccountService userAccountService;
	/**
	 * 复杂条件查询 查询条件为body中的json
	 * @param userAccountRecord
	 * @return
	 */
	@GetMapping()
	public ResponseData search(@RequestBody UserAccountRecord userAccountRecord,PageInfo page){
		List<UserAccountRecord> list = userAccountRecordService.search(userAccountRecord,page);
		if(list == null){
			return ResponseData.fail(ResponseCode.RESOURCE_NOT_FOUND);
		}
		return ResponseData.ok(list).appendPageInfo(page);
	}
	/**
	 * 充值
	 * @param userAccountRecord 只接收正数金额
	 * @return
	 */
	@PostMapping()
	public ResponseData recharge(@RequestBody UserAccountRecord userAccountRecord){
		//用户ID为空
		if(userAccountRecord.getUserId() == null || userAccountRecord.getUserId() <= 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		//充值金额为空
		if(userAccountRecord.getMoney() == null || userAccountRecord.getMoney() <= 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		//充值
		userAccountRecord.setBusinessType(AccountBusinessType.RECHARGE);
		Integer id = userAccountRecordService.rechargeCAS(userAccountRecord);
//		Integer id = userAccountRecordService.rechargePessimism(userAccountRecord);
		return ResponseData.ok(id);
	}
	
	/**
	 * 消费
	 * @param userAccountRecord 只接收负数金额
	 * @return
	 */
	@DeleteMapping()
	public ResponseData payment(@RequestBody UserAccountRecord userAccountRecord){
		//用户ID为空
		if(userAccountRecord.getUserId() == null || userAccountRecord.getUserId() <= 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		//充值金额为空
		if(userAccountRecord.getMoney() == null || userAccountRecord.getMoney() >= 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		//充值
		userAccountRecord.setBusinessType(AccountBusinessType.PAYMENT);
		Integer id = userAccountRecordService.rechargeCAS(userAccountRecord);
//		Integer id = userAccountRecordService.rechargePessimism(userAccountRecord);
		return ResponseData.ok(id);
	}
	
	
	/**
	 * 模拟并发错误，丢失更新
	 * @param userAccountRecord
	 * @return
	 */
//	@PostMapping()
//	public ResponseData lostModify(@RequestBody UserAccountRecord userAccountRecord){
//		//用户ID为空
//		if(userAccountRecord.getUserId() == null || userAccountRecord.getUserId() <= 0) {
//			throw new ControllerException(ResponseCode.ERROR_PARAM);
//		}
//		//充值金额为空
//		if(userAccountRecord.getMoney() == null || userAccountRecord.getMoney() <= 0) {
//			throw new ControllerException(ResponseCode.ERROR_PARAM);
//		}
//		//充值
//		userAccountRecord.setBusinessType(AccountBusinessType.RECHARGE);
//		Integer id = userAccountRecordService.rechargeOptimisticed(userAccountRecord);
//		return ResponseData.ok(id);
//	}
}
