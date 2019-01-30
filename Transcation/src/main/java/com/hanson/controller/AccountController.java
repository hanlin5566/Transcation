//package com.hanson.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.hanson.dao.gen.entity.SystemUser;
//import com.hanson.service.SystemUserService;
//import com.hzcf.base.response.ResponseData;
//
///**
// * Create by hanlin on 2019年1月30日
// **/
//@RestController()
//@RequestMapping(value = "/accounts")
//public class AccountController {
//	@Autowired
//	SystemUserService systemUserService;
//	/**
//	 * 给用户加钱,简单事务，如果发生异常则回滚
//	 * @param systemUser
//	 * @return
//	 */
//	@PutMapping("/")
//	public ResponseData addAccount(@RequestBody SystemUser systemUser){
//		Integer count = systemUserService.addAccount(systemUser);
//		return ResponseData.ok(count);
//	}
//}
