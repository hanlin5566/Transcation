package com.hanson.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Create by hanlin on 2019年1月28日
 **/
//@Configuration
public class WebContextConfiguration implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		// 注册ConverterFactory(类型转换器工厂)
		registry.addConverterFactory(new EnumConvertFactory());
	}
}
