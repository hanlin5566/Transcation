package com.hanson.dao.gen.mapper.ext;

import java.util.List;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

import com.hanson.dao.gen.entity.UserAccount;
import com.hanson.dao.gen.entity.UserAccountExample;

public interface UserAccountRecordExtMapper{
	@UpdateProvider(type=UserAccountSqlExtProvider.class, method="updateByCAS")
    int updateByCAS(UserAccount record);
	
	@UpdateProvider(type=UserAccountSqlExtProvider.class, method="updateByPessimism")
	int updateByPessimism(UserAccount record);
	
	@SelectProvider(type=UserAccountSqlExtProvider.class, method="selectByExampleForUpdate")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="balance", property="balance", jdbcType=JdbcType.BIGINT),
        @Result(column="version", property="version", jdbcType=JdbcType.TINYINT),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.TINYINT)
    })
    List<UserAccount> selectByExampleForUpdate(UserAccountExample example);
}