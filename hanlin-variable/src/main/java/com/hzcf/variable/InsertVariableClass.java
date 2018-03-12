package com.hzcf.variable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Create by hanlin on 2017年11月24日
 **/
@SpringBootApplication
public class InsertVariableClass {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(InsertVariableClass.class, args);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			String varName = "X_APP_A01";
			String filepath = "F:/workspace/HJ-Variable/target/classes/com/hzcf/variable/engine/algorithms/"+varName+".class";
			String className = "com.hzcf.variable.engine.algorithms."+varName;
			String classPaht = "F:\\workspace\\HJ-Variable\\target\\classes";
			JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
			con = jdbcTemplate.getDataSource().getConnection();
			String sql = "update derived_var v set v.clazz_name = ?, v.class_file = ?,v.clazz_path = ?,`state` = ? where v.var_ret_name = ?;";
			ps = con.prepareStatement(sql);
			InputStream in = new FileInputStream(filepath);// 生成被插入文件的节点流
			// 设置Blob
			ps.setString(1, className);
			ps.setBlob(2, in);
			ps.setString(3,classPaht);
			ps.setInt(4,3);
			ps.setString(5,varName);

			ps.executeUpdate();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
}
