//package com.hzcf.edge.common.aspect;
//
//import com.hzcf.edge.common.base.BasePublicService;
//import com.hzcf.ebs.entity.InterfaceRecordEntity;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 保存访问日志
// */
//@Aspect
//@Component
//public class ServiceLogAspect {
//
//    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
//
//    @Autowired
//    BasePublicService basePublicService;
//
//
//    @Pointcut("execution( * com.hzcf.api.*.service..*(..))")
//    public void servicePointCut() {
//    }
//
//    @AfterReturning(returning = "recordEntity", pointcut = "servicePointCut()")
//    public void doAfterReturning(InterfaceRecordEntity recordEntity) throws Throwable {
//          //统一访问日志存储 与 数据总线调用
//           basePublicService.addQueryLog(recordEntity);
//    }
//
//
//}
