package com.hanson.dao.gen.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hanson.base.enums.DataStatus;
import com.hzcf.base.serializer.EnumJsonSerializer;
import com.hzcf.base.util.DateUtils;

/**
 * tb_user_account 
 * @author huhanlin 2019-01-30
 */
public class UserAccount {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private Integer systemUserId;

    /**
     * 账户金额
     */
    private Long balance;

    /**
     * 
     */
    private Byte version;

    /**
     * 创建人
     */
    private Integer createUid;

    /**
     * 修改人
     */
    private Integer updateUid;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = DateUtils.ISO_DATE)
    private Date createTime;

    /**
     * 修改时间
     */
    @DateTimeFormat(pattern = DateUtils.ISO_DATE)
    private Date updateTime;

    /**
     * 数据状态（0.未知，1.正常，-1.删除)
     */
    @JsonSerialize(using = EnumJsonSerializer.class)
    private DataStatus dataStatus;

    /**
     * 
     * @return id 
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return system_user_id 
     */
    public Integer getSystemUserId() {
        return systemUserId;
    }

    /**
     * 
     * @param systemUserId 
     */
    public void setSystemUserId(Integer systemUserId) {
        this.systemUserId = systemUserId;
    }

    /**
     * 账户金额
     * @return balance 账户金额
     */
    public Long getBalance() {
        return balance;
    }

    /**
     * 账户金额
     * @param balance 账户金额
     */
    public void setBalance(Long balance) {
        this.balance = balance;
    }

    /**
     * 
     * @return version 
     */
    public Byte getVersion() {
        return version;
    }

    /**
     * 
     * @param version 
     */
    public void setVersion(Byte version) {
        this.version = version;
    }

    /**
     * 创建人
     * @return create_uid 创建人
     */
    public Integer getCreateUid() {
        return createUid;
    }

    /**
     * 创建人
     * @param createUid 创建人
     */
    public void setCreateUid(Integer createUid) {
        this.createUid = createUid;
    }

    /**
     * 修改人
     * @return update_uid 修改人
     */
    public Integer getUpdateUid() {
        return updateUid;
    }

    /**
     * 修改人
     * @param updateUid 修改人
     */
    public void setUpdateUid(Integer updateUid) {
        this.updateUid = updateUid;
    }

    /**
     * 创建时间
     * @return create_time 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 修改时间
     * @return update_time 修改时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 修改时间
     * @param updateTime 修改时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 数据状态（0.未知，1.正常，-1.删除)
     * @return data_status 数据状态（0.未知，1.正常，-1.删除)
     */
    public DataStatus getDataStatus() {
        return dataStatus;
    }

    /**
     * 数据状态（0.未知，1.正常，-1.删除)
     * @param dataStatus 数据状态（0.未知，1.正常，-1.删除)
     */
    public void setDataStatus(DataStatus dataStatus) {
        this.dataStatus = dataStatus;
    }
}