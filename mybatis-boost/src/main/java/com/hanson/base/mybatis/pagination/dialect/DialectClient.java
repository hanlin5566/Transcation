package com.hanson.base.mybatis.pagination.dialect;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hanson.base.mybatis.pagination.DB2Dialect;
import com.hanson.base.mybatis.pagination.H2Dialect;
import com.hanson.base.mybatis.pagination.HSQLDialect;
import com.hanson.base.mybatis.pagination.MySQLDialect;
import com.hanson.base.mybatis.pagination.OracleDialect;
import com.hanson.base.mybatis.pagination.PostgreSQLDialect;
import com.hanson.base.mybatis.pagination.SQLServer2005Dialect;
import com.hanson.base.mybatis.pagination.SQLServerDialect;
import com.hanson.base.mybatis.pagination.SybaseDialect;


/**
 * Create by hanlin on 2017年11月7日
 **/
public class DialectClient implements Serializable {
	private static final long serialVersionUID = 8107330250767760951L;
	private static final Map<DBMS, Dialect> DBMS_DIALECT = new HashMap<DBMS, Dialect>();

	/**
	 * 根据数据库名称获取数据库分页查询的方言实现。
	 *
	 * @param dbms 数据库名称
	 * @return 数据库分页方言实现
	 */
	public static Dialect getDbmsDialect(DBMS dbms) {
		if (DBMS_DIALECT.containsKey(dbms)) {
			return DBMS_DIALECT.get(dbms);
		}
		Dialect dialect = createDbmsDialect(dbms);
		DBMS_DIALECT.put(dbms, dialect);
		return dialect;
	}

	/**
	 * 插入自定义方言的实例
	 *
	 * @param exDialect 方言实现
	 */
	public static void putEx(Dialect exDialect) {
		DBMS_DIALECT.put(DBMS.EX, exDialect);
	}

	/**
	 * 创建数据库方言
	 *
	 * @param dbms 数据库
	 * @return 数据库
	 */
	private static Dialect createDbmsDialect(DBMS dbms) {
		switch (dbms) {
			case MYSQL:
				return new MySQLDialect();
			case ORACLE:
				return new OracleDialect();
			case DB2:
				return new DB2Dialect();
			case POSTGRE:
				return new PostgreSQLDialect();
			case SQLSERVER:
				return new SQLServerDialect();
			case SQLSERVER2005:
				return new SQLServer2005Dialect();
			case SYBASE:
				return new SybaseDialect();
			case H2:
				return new H2Dialect();
			case HSQL:
				return new HSQLDialect();
			default:
				throw new UnsupportedOperationException("Empty dbms dialect");
		}
	}


}
