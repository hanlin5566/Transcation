package com.hanson.component;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hanson.base.mybatis.enums.EnumType;

/**
 * Create by hanlin on 2019年1月28日
 **/

//@Component
public class EnumConvertFactory implements ConverterFactory<String, EnumType> {

	public <T extends EnumType> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToIEum<>(targetType);
	}

	private static class StringToIEum<T extends EnumType> implements Converter<String, T> {
		private Class<T> targerType;

		/**
		 * Instantiates a new String to i eum.
		 *
		 * @param targerType
		 *            the targer type
		 */
		public StringToIEum(Class<T> targerType) {
			this.targerType = targerType;
		}

		@Override
		public T convert(String source) {
			if (StringUtils.isEmpty(source)) {
				return null;
			}
			T[] enumConstants = targerType.getEnumConstants();
			for (T e : enumConstants) {
				if (e.text().equals(source))
					return e;
			}
			return null;
		}
	}
}
