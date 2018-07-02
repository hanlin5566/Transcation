package com.hzcf.edge.common.entity;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.edge.common.enums.ResponseEnums;
import com.hzcf.ebs.entity.InterfaceRecordEntity;

import java.io.Serializable;

import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

/**
 * Created by liqinwen on 2018/5/31.
 */
public class ResponseEntity implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code = ResponseEnums.codeOf(0);
    private Object data = null;
    private boolean success = true;
    private String message = "success";

    public ResponseEntity()
    {

    }

    private ResponseEntity(Object data)
    {
        if(data instanceof InterfaceRecordEntity)
        {
            InterfaceRecordEntity interfaceRecordEntity = (InterfaceRecordEntity)data;
            if("1".equals(interfaceRecordEntity.getState()))
            {
                this.success =false;
                this.message =interfaceRecordEntity.getErrorReturn();
                this.code= Integer.valueOf(ResponseEnums.SYSTEM_ERROR.toString());
            }else {
                this.setData(JSONObject.parseObject(interfaceRecordEntity.getResults()));
            }
        }else{
            this.data = data;
        }
    }

    private ResponseEntity(ResponseEnums code,String message)
    {
        this.success =false;
        this.message =message;
        this.code =ResponseEnums.codeOf(code.ordinal());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public  ResponseEntity build(Object data)
    {
        return new ResponseEntity(data);
    }

    public  ResponseEntity build(ResponseEnums code,String message)
    {
        return new ResponseEntity(code,message);
    }

}
