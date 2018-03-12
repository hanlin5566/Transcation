package com.hzcf.variable.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hzcf.variable.misc.IPUtils;
import com.hzcf.variable.model.Receive;
import com.hzcf.variable.model.Variable;
import com.hzcf.variable.service.VariableService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Create by hanlin on 2017年11月21日
 **/
@RestController
@RequestMapping(value = "/var")
@Api("衍生变量引擎")
public class VariableController {
	
	@Autowired VariableService varService;
	
	@ApiOperation(value = "执行衍生变量", notes = "根据传入的衍生接口名称，解析参数，返回衍生变量")
	@RequestMapping(value = { "" }, method = RequestMethod.POST)
	public Variable getDerivedVariableList(@RequestBody Receive rec,HttpServletRequest request) {
		try {
			rec.setRequestIP(IPUtils.getIpAddress(request));
		} catch (Exception e) {
		}
		return varService.execute(rec);
	}
	
	@Autowired JdbcTemplate jdbcTemplate;
	@ApiOperation(value = "插入衍生变量，只用于测试", notes = "根据传入的衍生名称，插入衍生变量，只用于测试。不能向外发布")
	@RequestMapping(value = { "/{varName}" }, method = RequestMethod.PUT)
	public void insert(@PathVariable String varName){
		Connection con = null;
		PreparedStatement ps = null;
		try {
			String filepath = "F:/workspace/HJ-Variable/target/classes/com/hzcf/variable/engine/algorithms/"+varName+".class";
			String className = "com.hzcf.variable.engine.algorithms."+varName;
//			String classPaht = "F:\\workspace\\HJ-Variable\\target\\classes";
			con = jdbcTemplate.getDataSource().getConnection();
			String sql = "update derived_var v set v.clazz_name = ?, v.class_file = ?,`state` = ? where v.var_ret_name = ?;";
			ps = con.prepareStatement(sql);
			InputStream in = new FileInputStream(filepath);// 生成被插入文件的节点流
			// 设置Blob
			ps.setString(1, className);
			ps.setBlob(2, in);
//			ps.setString(3,classPaht);
			ps.setInt(3,3);
			ps.setString(4,varName);

			ps.executeUpdate();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
}
