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
import com.hanson.dao.gen.entity.User;
import com.hanson.service.UserService;
import com.hzcf.base.exception.ControllerException;
//TODO:需要换成自己的responseCode
import com.hzcf.base.response.ResponseCode;
import com.hzcf.base.response.ResponseData;

/**
 * Create by hanlin on 2019年1月28日
 **/
@RestController()
@RequestMapping(value = "/api/users")
public class UserController {
	@Autowired
	UserService userService;
	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseData get(@PathVariable Integer id){
		User user = userService.get(id);
		if(user == null){
			return ResponseData.fail(ResponseCode.RESOURCE_NOT_FOUND);
		}
		return ResponseData.ok(user);
	}
	/**
	 * 复杂条件查询 查询条件为body中的json
	 * @param user
	 * @return
	 */
	@GetMapping()
	public ResponseData search(@RequestBody User user,PageInfo page){
		List<User> list = userService.search(user,page);
		if(list == null){
			return ResponseData.fail(ResponseCode.RESOURCE_NOT_FOUND);
		}
		return ResponseData.ok(list).appendPageInfo(page);
	}
	/**
	 * 新增
	 * @param user
	 * @return
	 */
	@PostMapping()
	public ResponseData add(@RequestBody User user){
		Integer id = userService.insert(user);
		return ResponseData.ok(id);
	}
	/**
	 * 修改信息
	 * @param user
	 * @return
	 */
	@PutMapping()
	public ResponseData update(@RequestBody User user){
		Integer id = user.getId();
		if(id == null || id < 0) {
			throw new ControllerException(ResponseCode.ERROR_PARAM);
		}
		Integer count = userService.update(user);
		return ResponseData.ok(count);
	}
	
	/**
	 * 删除信息
	 * @param user
	 * @return
	 */
	@DeleteMapping("/{id}")
	//TODO:由于修改未使用bean,所以createid/updateid/无法自动写入
	public ResponseData delete(@PathVariable Integer id){
		Integer count = userService.delete(id);
		return ResponseData.ok(count);
	}
}
