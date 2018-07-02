package com.hzcf.edge.common.utils;

import com.hzcf.edge.common.conf.ApplicationContextRegister;
import com.hzcf.ebs.entity.InterfaceRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liqinwen on 2017/9/15.
 */
public class PublicReflect {

    private static Logger logger = LoggerFactory.getLogger(PublicReflect.class);

    private static ApplicationContext wac = ApplicationContextRegister.getApplicationContext();

    public static InterfaceRecordEntity invoke(String serviceName, String method, InterfaceRecordEntity recordEntity) {
        Method m = null;
        try {
            Class cls = wac.getBean(serviceName).getClass();
            try {
                m = cls.getDeclaredMethod(method, InterfaceRecordEntity.class);
            } catch (NoSuchMethodException e) {
                logger.error(serviceName + ":" + method + " 接口服务不存在 NoSuchMethodException" + e.getMessage());
            }
            recordEntity = (InterfaceRecordEntity) m.invoke(wac.getBean(serviceName), recordEntity);
        } catch (IllegalAccessException e) {
            logger.error(serviceName + ":" + method + " 接口服务调用异常 IllegalAccessException" + e.getMessage());

        } catch (InvocationTargetException e) {
            logger.error(serviceName + ":" + method + " 接口服务调用异常 InvocationTargetException " + e.getMessage());
        }
        return recordEntity;
    }
}
