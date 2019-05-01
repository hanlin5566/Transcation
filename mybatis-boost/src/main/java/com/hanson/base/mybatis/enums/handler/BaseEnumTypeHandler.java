package com.hanson.base.mybatis.enums.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanson.base.enums.EnumType;


/**
 * Create by hanlin on 2017年11月7日
 * 自定义枚举类型转换器
 **/
public class BaseEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Class<E> type;
	private Map<Integer, E> map = new HashMap<Integer, E>();
	
	public BaseEnumTypeHandler() {} 
	
	public BaseEnumTypeHandler(Class<E> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}
		this.type = type;
		E[] enums = type.getEnumConstants();
		if (enums == null) {
			throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
		}
		for (E e : enums) {
			EnumType enumType = (EnumType) e;
			map.put(enumType.code(), e);
		}
	}
	/**
	 * 参数不为空时，当参数类型是枚举类型时，则将code存入数据库
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		EnumType enumType = (EnumType) parameter;
		ps.setInt(i, enumType.code());
	}
	/**
	 * 结果不为空时根据code转换成枚举
	 */
	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		int i = rs.getInt(columnName);
		if (rs.wasNull()) {
			return null;
		} else {
			return getEnumType(i);
		}
	}
	/**
	 * 用于定义通过字段索引获取字段数据时，如何把数据库类型转换为对应的Java类型
	 */
	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		int i = rs.getInt(columnIndex);
		if (rs.wasNull()) {
			return null;
		} else {
			return getEnumType(i);
		}
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		int i = cs.getInt(columnIndex);
		if (cs.wasNull()) {
			return null;
		} else {
			return getEnumType(i);
		}
	}

	private E getEnumType(int value) {
		E result = null;
		try {
			result = map.get(value);
			if (null == result) {
				throw new Exception();
			}
		} catch (Exception ex) {
			logger.error("Cannot convert {} to {} by value.", value, type.getSimpleName());
		}
		return map.get(value);
	}
}