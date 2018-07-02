package com.hzcf.edge.common.handler;


import com.hzcf.edge.common.entity.ResponseEntity;
import com.hzcf.edge.common.enums.ResponseEnums;
import com.hzcf.edge.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by liqinwen on 2018/5/31.
 */
@ControllerAdvice
@ResponseBody
public class CustomExceptionHandler {


    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        return new ResponseEntity().build(ResponseEnums.BAD_REQUEST,e.getLocalizedMessage());
    }

    /**
     * 405 - Method Not Allowed
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return  new ResponseEntity().build(ResponseEnums.METHOD_NOT_ALLOWED,e.getLocalizedMessage());
    }

    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        e.printStackTrace();
        return  new ResponseEntity().build(ResponseEnums.SYSTEM_ERROR,e.getMessage());
    }

    /**
     * 自定义异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(value = CustomException.class)
    @ResponseBody
    public ResponseEntity defaultErrorHandler(HttpServletRequest request, CustomException e) {


        return  new ResponseEntity().build(e.getCode(),e.getMessage());
    }
}
