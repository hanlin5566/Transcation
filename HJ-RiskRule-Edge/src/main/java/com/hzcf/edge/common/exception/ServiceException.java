package com.hzcf.edge.common.exception;

import com.hzcf.ebs.entity.InterfaceRecordEntity;

public class ServiceException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = "系统异常";
    private String errorCode = "500000";
    private InterfaceRecordEntity interfaceRecordEntity = null;

    public InterfaceRecordEntity getInterfaceRecordEntity() {
        return interfaceRecordEntity;
    }

    public void setInterfaceRecordEntity(InterfaceRecordEntity interfaceRecordEntity) {
        this.interfaceRecordEntity = interfaceRecordEntity;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String errorCode,String message) {
       // super(message);
        this.message = message;
        this.errorCode = errorCode;
    }

    public ServiceException(String errorCode,String message,InterfaceRecordEntity interfaceRecordEntity)
    {
        this.message = message;
        this.errorCode = errorCode;
        this.interfaceRecordEntity = interfaceRecordEntity;
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
     }
}
