package com.hzcf.edge.common.exception;

import com.hzcf.edge.common.enums.ResponseEnums;

/**
 * Created by liqinwen on 2018/5/31.
 */
public class CustomException extends RuntimeException{

    private String message;
    private ResponseEnums code;

    public CustomException (String message)
    {
        super(message);
        this.message=message;
    }

    public CustomException (String message,Exception e)
    {
        super(message,e);
        this.message=message;
    }

    public CustomException(ResponseEnums code,String message)
    {
        super(message);
        this.message=message;
        this.code=code;
    }

    public CustomException(ResponseEnums code,String message,Exception e)
    {
        super(message,e);
        this.message=message;
        this.code=code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseEnums getCode() {
        return code;
    }

    public void setCode(ResponseEnums code) {
        this.code = code;
    }

}
