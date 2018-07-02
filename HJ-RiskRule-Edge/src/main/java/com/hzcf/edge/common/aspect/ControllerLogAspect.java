//package com.hzcf.edge.common.aspect;
//
//import com.hzcf.edge.common.base.BasePublicService;
//import com.hzcf.ebs.entity.InterfaceRecordEntity;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * 保存访问日志
// */
//@Aspect
//@Component
//public class ControllerLogAspect {
//
//    private static final Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);
//
//    @Autowired
//    BasePublicService basePublicService;
//
//    @Pointcut("execution( * com.hzcf.api.task.controller..*(..))")
//    public void logPointCut() {
//    }
//
//
//    @Before("logPointCut()")
//    public void doBefore(JoinPoint joinPoint) throws Throwable {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        InterfaceRecordEntity interfaceRecordEntity = basePublicService.before(request);
//        Object[] obj = joinPoint.getArgs();
//        for (Object argItem : obj) {
//            if (argItem instanceof InterfaceRecordEntity) {
//                InterfaceRecordEntity var1 = (InterfaceRecordEntity) argItem;
//                BeanUtils.copyProperties(interfaceRecordEntity,var1);
//            }
//        }
//    }
//}
