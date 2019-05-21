package com.hanson.base.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hanson.base.annotation.AutoWriteParam;
import com.hanson.dao.gen.entity.User;

/**
 * Create by hanlin on 2017年11月14日 基于AOP记录controller的行为日志
 **/
@Aspect
@Component
// 加载顺序 需要保证在 spring的事物之前执行，才会进入@AfterThrowing
@Order(1)
public class AutoWriteParamAspect {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * ~第一个 * 代表任意修饰符及任意返回值. ~第二个 * 任意包名 ~第三个 * 代表任意方法. ~第四个 * 定义子包 ~第五个(..) 任意方法 ~
	 * ..匹配任意数量的参数.
	 * 定义拦截规则：拦截com.hanson.operation.controller包下面的所有类中，有@RequestMapping注解的方法。
	 */
	@Pointcut("execution(* com.hanson.controller..*(..)) and @annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void addUserIdAspect() {
	}

	/**
	 * 拦截器具体实现
	 *
	 * @param joinPoint
	 * @return JsonResult（被拦截方法的执行结果，或需要登录的错误提示。）
	 * @throws Throwable
	 */
	@Around("addUserIdAspect()") // 指定拦截器规则；也可以直接把"execution(*// com.hanson………)"写进这里
	public Object Interceptor(ProceedingJoinPoint joinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		// 解析参数
		Object[] args = joinPoint.getArgs();
		for (Object arg : args) {
			if (arg.getClass().isAnnotationPresent(AutoWriteParam.class)) {
				addDefaultVal(request, arg);
			}
		}
		return joinPoint.proceed();
	}

	private void addDefaultVal(HttpServletRequest request, Object argentity) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// SystemUser userInfo = (SystemUser)
		// request.getSession().getAttribute(SessionInterceptor.SESSION_USER);
		User userInfo = new User();
		userInfo.setId(20080);
		if (userInfo != null) {
			// SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.ISO_DATE_TIME);
			// TODO:invoke setCompanyCode
			// Method method = entity.getClass().getMethod("setCompanyCode", String.class);
			// method.invoke(entity, userInfo.getCompanyCode());
			Method createUserIdMethod = argentity.getClass().getMethod("setCreateUid", Integer.class);
			Method updateUserIdMethod = argentity.getClass().getMethod("setUpdateUid", Integer.class);
			switch (request.getMethod().toUpperCase()) {
			case "POST":
				// POST 新增操作
				createUserIdMethod.invoke(argentity, userInfo.getId());
				updateUserIdMethod.invoke(argentity, userInfo.getId());
				// request.setAttribute("createTime", sdf.format(new Date()));
				// request.setAttribute("updateTime", sdf.format(new Date()));
				break;
			case "PUT":
				// PUT 更新操作
				updateUserIdMethod.invoke(argentity, userInfo.getId());
				// request.setAttribute("updateTime", sdf.format(new Date()));
				break;
			case "DELETE":
				// DELETE 删除操作
				updateUserIdMethod.invoke(argentity, userInfo.getId());
				// request.setAttribute("updateTime", sdf.format(new Date()));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 使用@AfterThrowing 用来处理当切入内容部分抛出异常之后的处理逻辑。
	 *
	 * @param joinPoint
	 */
	@AfterThrowing(pointcut = "addUserIdAspect()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
		logger.error("@doAfterThrowing", e);
	}
}
