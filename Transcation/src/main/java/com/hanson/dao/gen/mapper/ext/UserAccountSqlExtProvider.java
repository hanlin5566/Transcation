package com.hanson.dao.gen.mapper.ext;

import org.apache.ibatis.jdbc.SQL;

import com.hanson.dao.gen.entity.UserAccount;
import com.hanson.dao.gen.entity.UserAccountExample;
import com.hanson.dao.gen.mapper.UserAccountSqlProvider;

public class UserAccountSqlExtProvider extends UserAccountSqlProvider{
    public String updateByCAS(UserAccount record) {
        SQL sql = new SQL();
        sql.UPDATE("tb_user_account");
        
        if (record.getUserId() != null) {
            sql.SET("user_id = #{userId,jdbcType=INTEGER}");
        }
        
        if (record.getBalance() != null) {
            sql.SET("balance = #{balance,jdbcType=BIGINT}");
        }
        //TODO:version++ CAS
        sql.SET("version = version+1");
        
        if (record.getCreateUid() != null) {
            sql.SET("create_uid = #{createUid,jdbcType=INTEGER}");
        }
        
        if (record.getUpdateUid() != null) {
            sql.SET("update_uid = #{updateUid,jdbcType=INTEGER}");
        }
        
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getUpdateTime() != null) {
            sql.SET("update_time = #{updateTime,jdbcType=TIMESTAMP}");
        }
        
        if (record.getDataStatus() != null) {
            sql.SET("data_status = #{dataStatus,jdbcType=TINYINT}");
        }
        
        sql.WHERE("id = #{id,jdbcType=INTEGER}", "version = #{version,jdbcType=TINYINT}");
        
        return sql.toString();
    }
    
    public String updateByPessimism(UserAccount record) {
    	SQL sql = new SQL();
    	sql.UPDATE("tb_user_account");
    	
    	if (record.getUserId() != null) {
    		sql.SET("user_id = #{userId,jdbcType=INTEGER}");
    	}
    	
    	if (record.getBalance() != null) {
    		sql.SET("balance = #{balance,jdbcType=BIGINT}");
    	}
    	//TODO:version++ CAS
    	sql.SET("version = version+1");
    	
    	if (record.getCreateUid() != null) {
    		sql.SET("create_uid = #{createUid,jdbcType=INTEGER}");
    	}
    	
    	if (record.getUpdateUid() != null) {
    		sql.SET("update_uid = #{updateUid,jdbcType=INTEGER}");
    	}
    	
    	if (record.getCreateTime() != null) {
    		sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
    	}
    	
    	if (record.getUpdateTime() != null) {
    		sql.SET("update_time = #{updateTime,jdbcType=TIMESTAMP}");
    	}
    	
    	if (record.getDataStatus() != null) {
    		sql.SET("data_status = #{dataStatus,jdbcType=TINYINT}");
    	}
    	
    	sql.WHERE("id = #{id,jdbcType=INTEGER}", "version = #{version,jdbcType=TINYINT}");
    	
    	return sql.toString();
    }
    public String selectByExampleForUpdate(UserAccountExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("user_id");
        sql.SELECT("balance");
        sql.SELECT("version");
        sql.SELECT("create_uid");
        sql.SELECT("update_uid");
        sql.SELECT("create_time");
        sql.SELECT("update_time");
        sql.SELECT("data_status");
        sql.FROM("tb_user_account");
        applyWhere(sql, example, false);
        
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        String string = sql.toString();
        return string+" FOR UPDATE";
    }
    
}