package com.hanson.dao.gen.mapper;

import com.hanson.dao.gen.entity.UserAccountRecord;
import com.hanson.dao.gen.entity.UserAccountRecordExample;
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

public interface UserAccountRecordMapper {
    @Delete({
        "delete from tb_user_account_record",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into tb_user_account_record (user_id, money, ",
        "balance, business_type, ",
        "version, create_uid, ",
        "update_uid, create_time, ",
        "update_time, data_status)",
        "values (#{userId,jdbcType=INTEGER}, #{money,jdbcType=BIGINT}, ",
        "#{balance,jdbcType=BIGINT}, #{businessType,jdbcType=TINYINT}, ",
        "#{version,jdbcType=TINYINT}, #{createUid,jdbcType=INTEGER}, ",
        "#{updateUid,jdbcType=INTEGER}, #{createTime,jdbcType=TIMESTAMP}, ",
        "#{updateTime,jdbcType=TIMESTAMP}, #{dataStatus,jdbcType=TINYINT})"
    })
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=Integer.class)
    int insert(UserAccountRecord record);

    @InsertProvider(type=UserAccountRecordSqlProvider.class, method="insertSelective")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=Integer.class)
    int insertSelective(UserAccountRecord record);

    @SelectProvider(type=UserAccountRecordSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="money", property="money", jdbcType=JdbcType.BIGINT),
        @Result(column="balance", property="balance", jdbcType=JdbcType.BIGINT),
        @Result(column="business_type", property="businessType", jdbcType=JdbcType.TINYINT),
        @Result(column="version", property="version", jdbcType=JdbcType.TINYINT),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.TINYINT)
    })
    List<UserAccountRecord> selectByExampleWithRowbounds(UserAccountRecordExample example, RowBounds rowBounds);

    @SelectProvider(type=UserAccountRecordSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="money", property="money", jdbcType=JdbcType.BIGINT),
        @Result(column="balance", property="balance", jdbcType=JdbcType.BIGINT),
        @Result(column="business_type", property="businessType", jdbcType=JdbcType.TINYINT),
        @Result(column="version", property="version", jdbcType=JdbcType.TINYINT),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.TINYINT)
    })
    List<UserAccountRecord> selectByExample(UserAccountRecordExample example);

    @Select({
        "select",
        "id, user_id, money, balance, business_type, version, create_uid, update_uid, ",
        "create_time, update_time, data_status",
        "from tb_user_account_record",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_id", property="userId", jdbcType=JdbcType.INTEGER),
        @Result(column="money", property="money", jdbcType=JdbcType.BIGINT),
        @Result(column="balance", property="balance", jdbcType=JdbcType.BIGINT),
        @Result(column="business_type", property="businessType", jdbcType=JdbcType.TINYINT),
        @Result(column="version", property="version", jdbcType=JdbcType.TINYINT),
        @Result(column="create_uid", property="createUid", jdbcType=JdbcType.INTEGER),
        @Result(column="update_uid", property="updateUid", jdbcType=JdbcType.INTEGER),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="update_time", property="updateTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="data_status", property="dataStatus", jdbcType=JdbcType.TINYINT)
    })
    UserAccountRecord selectByPrimaryKey(Integer id);

    @UpdateProvider(type=UserAccountRecordSqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(UserAccountRecord record);

    @Update({
        "update tb_user_account_record",
        "set user_id = #{userId,jdbcType=INTEGER},",
          "money = #{money,jdbcType=BIGINT},",
          "balance = #{balance,jdbcType=BIGINT},",
          "business_type = #{businessType,jdbcType=TINYINT},",
          "version = #{version,jdbcType=TINYINT},",
          "create_uid = #{createUid,jdbcType=INTEGER},",
          "update_uid = #{updateUid,jdbcType=INTEGER},",
          "create_time = #{createTime,jdbcType=TIMESTAMP},",
          "update_time = #{updateTime,jdbcType=TIMESTAMP},",
          "data_status = #{dataStatus,jdbcType=TINYINT}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(UserAccountRecord record);
}