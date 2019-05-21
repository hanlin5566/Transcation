package com.hanson.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanson.base.mybatis.pagination.entity.PageInfo;
import com.hanson.dao.gen.entity.UserAccount;
import com.hanson.service.UserAccountService;
import com.hanson.base.exception.ControllerException;
//TODO:需要换成自己的responseCode
import com.hanson.base.response.ResponseCode;
import com.hanson.base.response.ResponseData;

/**
 * Create by hanlin on 2019年1月28日
 **/
@RestController()
@RequestMapping(value = "/api/userAccounts")
public class UserAccountController {
	@Autowired
	UserAccountService userAccountService;
	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseData get(@PathVariable Integer id){
		UserAccount userAccount = userAccountService.get(id);
		if(userAccount == null){
			return ResponseData.fail(ResponseCode.RESOURCE_NOT_FOUND);
		}
		return ResponseData.ok(userAccount);
	}
	/**
	 * 复杂条件查询 查询条件为body中的json
	 * @param userAccount
	 * @return
	 */
	@GetMapping()
	public ResponseData search(@RequestBody UserAccount userAccount,PageInfo page){
		List<UserAccount> list = userAccountService.search(userAccount,page);
		if(list == null){
			return ResponseData.fail(ResponseCode.RESOURCE_NOT_FOUND);
		}
		return ResponseData.ok(list).appendPageInfo(page);
	}
	/**
	 * 新增
	 * @param userAccount
	 * @return
	 */
	@PostMapping()
	public ResponseData add(@RequestBody UserAccount userAccount){
		Integer id = userAccountService.insert(userAccount);
		return ResponseData.ok(id);
	}
	/**
	 * 修改信息
	 * @param userAccount
	 * @return
	 */
	@PutMapping()
	public ResponseData update(@RequestBody UserAccount userAccount){
		Integer id = userAccount.getId();
		if(id == null || id < 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		Integer count = userAccountService.update(userAccount);
		return ResponseData.ok(count);
	}
	
	/**
	 * 删除信息
	 * @param userAccount
	 * @return
	 */
	@DeleteMapping("/{id}")
	public ResponseData delete(@PathVariable Integer id){
		Integer count = userAccountService.delete(id);
		return ResponseData.ok(count);
	}
}
