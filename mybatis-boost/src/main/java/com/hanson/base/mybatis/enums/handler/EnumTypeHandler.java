package com.hanson.base.mybatis.enums.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.hanson.base.enums.EnumType;

/**
 * Create by hanlin on 2017年11月7日
 **/
public class EnumTypeHandler<E extends Enum<E>> extends BaseEnumTypeHandler<E> {
	private BaseTypeHandler<E> typeHandler = null;

	public EnumTypeHandler(Class<E> type) {
		super(type);
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}
		if (EnumType.class.isAssignableFrom(type)) {
			// 如果实现了 BaseCodeEnum 则使用我们自定义的转换器
			typeHandler = new BaseEnumTypeHandler<E>(type);
		} else {
			// 默认转换器 也可换成 EnumOrdinalTypeHandler
			typeHandler = new EnumOrdinalTypeHandler<E>(type);
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		typeHandler.setNonNullParameter(ps, i, parameter, jdbcType);
	}

	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return (E) typeHandler.getNullableResult(rs, columnName);
	}

	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return (E) typeHandler.getNullableResult(rs, columnIndex);
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return (E) typeHandler.getNullableResult(cs, columnIndex);
	}
}
