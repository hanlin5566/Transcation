package com.hanson.base.mybatis.config;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hanson.base.enums.EnumType;
import com.hanson.base.mybatis.enums.handler.EnumTypeHandler;
import com.hanson.base.mybatis.pagination.helper.PaginationInterceptor;
import com.hanson.base.util.ClassUtils;

/**
 * Create by hanlin on 2019年1月28日
 **/
@Configuration
@ConfigurationProperties(prefix = "mybatis")
public class MyBatisConfig {

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource,PaginationInterceptor paginationInterceptor) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setPlugins(new Interceptor[]{paginationInterceptor});
		SqlSessionFactory sqlSessionFactory = factory.getObject();

		// 取得类型转换注册器
		TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
		// 注册默认枚举转换器
		typeHandlerRegistry.setDefaultEnumTypeHandler(EnumTypeHandler.class);
		final List<Class<?>> allAssignedClass = ClassUtils.getAllAssignedClass(EnumType.class);
		allAssignedClass.forEach((clazz) -> typeHandlerRegistry.register(clazz, EnumTypeHandler.class));

		return sqlSessionFactory;
	}

	/**
	 * 分页配置
	 * @return
	 */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor pageHelper = new PaginationInterceptor();
	    Properties p = new Properties();
	    p.setProperty("dbms", "mysql");
	    p.setProperty("sqlRegex", ".*WithRowbounds*");
	    pageHelper.setProperties(p);
	    return pageHelper;
	}
}
