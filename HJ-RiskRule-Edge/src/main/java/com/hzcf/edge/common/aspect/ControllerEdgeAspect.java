//package com.hzcf.edge.common.aspect;
//
//import java.util.Enumeration;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import com.alibaba.fastjson.JSONObject;
//
///**
// * 保存访问日志
// */
//@Aspect
//@Component
//public class ControllerEdgeAspect {
//
//	private static final Logger logger = LoggerFactory.getLogger(ControllerEdgeAspect.class);
//
//	@Pointcut("execution( * com.hzcf.edge.controller..*(..))")
//	public void edgePointCut() {
//		// 边缘服务前置操作。
//	}
//
//	@Before("edgePointCut()")
//	public void doBefore(JoinPoint joinPoint) throws Throwable {
//		// 边缘服务后置操作
//	}
//
//	@After("edgePointCut()")
//	public void doAfter(JoinPoint joinPoint) throws Throwable {
//		// 边缘服务后置操作,先于@AfterReturning执行
//	}
//
//	@AfterReturning(returning = "results", pointcut = "edgePointCut()")
//	public void doAfterReturning(Object results) throws Throwable {
//		// 边缘服务执行完毕后执行
//	}
//
//	@Around("edgePointCut()")
//	public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
//		preHandle();
//
//		Object retVal = joinPoint.proceed();
//
//		postHandle(retVal);
//
//		return retVal;
//	}
//
//	private void preHandle() {
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
//				.getRequest();
//		
//		StringBuffer sb = new StringBuffer();
//		sb.append("{");
//		
//		Enumeration<String> headers = request.getHeaderNames();
//		int i = 0;
//		while (headers.hasMoreElements()) {
//			String header = headers.nextElement();
//			
//			if (i > 0)
//				sb.append(", ");
//			sb.append(header + ": " + request.getHeader(header));
//			i++;
//		}
//		sb.append("}");
//		
//		logger.debug("Pre handling request: {}, headers: {}", getRequestInfo(request, true), sb.toString());
//	}
//
//	private void postHandle(Object retVal) {
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
//				.getRequest();
//		logger.debug("Post handling request: {}, response: {}", getRequestInfo(request, false),
//				JSONObject.toJSONString(retVal));
//	}
//
//	private String getRequestInfo(HttpServletRequest request, boolean requestDetails) {
//		StringBuffer sb = new StringBuffer();
//		sb.append(request.getMethod()).append(" ");
//		sb.append(request.getRequestURI());
//		if (requestDetails) {
//			Enumeration<String> e = request.getParameterNames();
//			sb.append(" ").append("{");
//			int i = 0;
//			while (e.hasMoreElements()) {
//				String name = e.nextElement();
//				String val = request.getParameter(name);
//
//				if (val != null && !val.isEmpty()) {
//					if (i > 0)
//						sb.append(", ");
//					sb.append(name).append(": ").append(val);
//
//					i++;
//				}
//			}
//			sb.append("}");
//		}
//
//		return sb.toString();
//	}
//}
