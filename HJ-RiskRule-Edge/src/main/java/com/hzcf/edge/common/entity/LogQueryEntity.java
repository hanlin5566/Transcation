package com.hzcf.edge.common.entity;

import com.hzcf.ebs.entity.InterfaceRecordEntity;

/**
 * Created by liqinwen on 2017/9/14.
 */
public class LogQueryEntity {

    private String _id;
    private InterfaceRecordEntity message;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public InterfaceRecordEntity getInterfaceRecordEntity() {
        return message;
    }

    public void setInterfaceRecordEntity(InterfaceRecordEntity interfaceRecordEntity) {
        this.message = interfaceRecordEntity;
    }
}