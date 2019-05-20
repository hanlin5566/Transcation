package com.hanson.dao.gen.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;

import com.hanson.dao.gen.entity.SystemUser;
import com.hanson.dao.gen.entity.SystemUserExample;

public interface SystemUserMapper {
    @Delete({
        "delete from system_user",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into system_user (sex, user_name, ",
        "api_name, api_pwd, ",
        "dept_id, user_pwd, ",
        "user_salt, user_email, ",
        "user_tel, user_address, ",
        "user_phone, company_code, ",
        "create_uid, update_uid, ",
        "create_time, update_time, ",
        "data_status)",
        "values (#{sex,jdbcType=VARCHAR}, #{userName,jdbcType=VARCHAR}, ",
        "#{apiName,jdbcType=VARCHAR}, #{apiPwd,jdbcType=VARCHAR}, ",
        "#{deptId,jdbcType=VARCHAR}, #{userPwd,jdbcType=VARCHAR}, ",
        "#{userSalt,jdbcType=VARCHAR}, #{userEmail,jdbcType=VARCHAR}, ",
        "#{userTel,jdbcType=VARCHAR}, #{userAddress,jdbcType=VARCHAR}, ",
        "#{userPhone,jdbcType=VARCHAR}, #{companyCode,jdbcType=VARCHAR}, ",
        "#{createUid,jdbcType=INTEGER}, #{updateUid,jdbcType=INTEGER}, ",
        "#{createTime,jdbcType=DATE}, #{updateTime,jdbcType=DATE}, ",
        "#{dataStatus,jdbcType=INTEGER})"
    })
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=Integer.class)
    int insert(SystemUser record);

    @InsertProvider(type=SystemUserSqlProvider.class, method="insertSelective")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=Integer.class)
    int insertSelective(SystemUser record);

    @SelectProvider(type=SystemUserSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="sex", property="sex", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_name", property="userName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_name", property="apiName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_pwd", property="apiPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="dept_id", property="deptId", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_pwd", property="userPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_salt", property="userSalt", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_email", property="userEmail", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_tel", property="userTel", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_address", property="userAddress", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_phone", property="userPhone", jdbcType=JdbcType.VARCHAR),
        @Result(column="company_code", property="companyCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.DATE),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.DATE),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.INTEGER)
    })
    List<SystemUser> selectByExampleWithRowbounds(SystemUserExample example, RowBounds rowBounds);

    @SelectProvider(type=SystemUserSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="sex", property="sex", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_name", property="userName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_name", property="apiName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_pwd", property="apiPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="dept_id", property="deptId", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_pwd", property="userPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_salt", property="userSalt", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_email", property="userEmail", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_tel", property="userTel", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_address", property="userAddress", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_phone", property="userPhone", jdbcType=JdbcType.VARCHAR),
        @Result(column="company_code", property="companyCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.DATE),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.DATE),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.INTEGER)
    })
    List<SystemUser> selectByExample(SystemUserExample example);

    @Select({
        "select",
        "id, sex, user_name, api_name, api_pwd, dept_id, user_pwd, user_salt, user_email, ",
        "user_tel, user_address, user_phone, company_code, create_uid, update_uid, create_time, ",
        "update_time, data_status",
        "from system_user",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="sex", property="sex", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_name", property="userName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_name", property="apiName", jdbcType=JdbcType.VARCHAR),
        @Result(column="api_pwd", property="apiPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="dept_id", property="deptId", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_pwd", property="userPwd", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_salt", property="userSalt", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_email", property="userEmail", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_tel", property="userTel", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_address", property="userAddress", jdbcType=JdbcType.VARCHAR),
        @Result(column="user_phone", property="userPhone", jdbcType=JdbcType.VARCHAR),
        @Result(column="company_code", property="companyCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.DATE),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.DATE),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.INTEGER)
    })
    SystemUser selectByPrimaryKey(Integer id);

    @UpdateProvider(type=SystemUserSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(SystemUser record);

    @Update({
        "update system_user",
        "set sex = #{sex,jdbcType=VARCHAR},",
          "user_name = #{userName,jdbcType=VARCHAR},",
          "api_name = #{apiName,jdbcType=VARCHAR},",
          "api_pwd = #{apiPwd,jdbcType=VARCHAR},",
          "dept_id = #{deptId,jdbcType=VARCHAR},",
          "user_pwd = #{userPwd,jdbcType=VARCHAR},",
          "user_salt = #{userSalt,jdbcType=VARCHAR},",
          "user_email = #{userEmail,jdbcType=VARCHAR},",
          "user_tel = #{userTel,jdbcType=VARCHAR},",
          "user_address = #{userAddress,jdbcType=VARCHAR},",
          "user_phone = #{userPhone,jdbcType=VARCHAR},",
          "company_code = #{companyCode,jdbcType=VARCHAR},",
          "create_uid = #{createUid,jdbcType=INTEGER},",
          "update_uid = #{updateUid,jdbcType=INTEGER},",
          "create_time = #{createTime,jdbcType=DATE},",
          "update_time = #{updateTime,jdbcType=DATE},",
          "data_status = #{dataStatus,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SystemUser record);
}