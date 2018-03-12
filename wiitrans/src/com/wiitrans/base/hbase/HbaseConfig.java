package com.wiitrans.base.hbase;


public class HbaseConfig {

	public static String HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT = "2181";
	public static String HBASE_ZOOKEEPER_QUORUM = "master";
	public static String HBASE_MASTER = "127.0.0.1:600000";

	public static String HBASE_COLUMN_FAMILY_MSG_CONTENT = "msgContent";
	public static String HBASE_TABLE_PREFIX_ROOM = "room";

	public static String HBASE_ADMIN_ORDER_LOG_INDEX = "admin_orderlog_index";
	public static String HBASE_ADMIN_ORDER_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ADMIN_ORDER_LOG_TABLE_PREFIX = "admin_orderlog_1k_";

	public static String HBASE_ADMIN_USER_LOG_INDEX = "admin_userlog_index";
	public static String HBASE_ADMIN_USER_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ADMIN_USER_LOG_TABLE_PREFIX = "admin_userlog_1k_";

	public static String HBASE_ADMIN_FINANCE_LOG_INDEX = "admin_financelog_index";
	public static String HBASE_ADMIN_FINANCE_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ADMIN_FINANCE_LOG_TABLE_PREFIX = "admin_financelog_1k_";

	public static String HBASE_ADMIN_OPERATE_LOG_INDEX = "admin_operatelog_index";
	public static String HBASE_ADMIN_OPERATE_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ADMIN_OPERATE_LOG_TABLE_PREFIX = "admin_operatelog_1k_";

	public static String HBASE_ADMIN_SYSTEM_LOG_INDEX = "admin_systemlog_index";
	public static String HBASE_ADMIN_SYSTEM_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ADMIN_SYSTEM_LOG_TABLE_PREFIX = "admin_systemlog_1k_";

	public static String HBASE_GRADE_TEST_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_GRADE_TEST_LOG_TABLE = "grade_testlog";

	public static String HBASE_ORDER_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_ORDER_LOG_TABLE_PREFIX = "ordercycle_";

	public static String HBASE_USER_ACTION_LOG_COLUMN_FAMILY = "log";
	public static String HBASE_USER_ACTION_LOG_TABLE_PREFIX = "useraction";

	public static String HBASE_TM_TEXT_COLUMN_FAMILY = "tu";
	public static String HBASE_TM_TEXT_TABLE_PREFIX = "tm_text_";

	public static String HBASE_TM_TU_WORD_INDEX_COLUMN_FAMILY = "index";
	public static String HBASE_TM_TU_WORD_INDEX_TABLE_PREFIX = "tm_tu_word_";

	public static String HBASE_TM_WORD_TU_INDEX_COLUMN_FAMILY = "index";
	public static String HBASE_TM_WORD_TU_INDEX_TABLE_PREFIX = "tm_word_tu_";

	public static String HBASE_TM_WORD_TIME_COLUMN_FAMILY = "time";
	public static String HBASE_TM_WORD_TIME_TABLE_PREFIX = "tm_word_time_";

	public static String HBASE_TM_WORD_LONGER_COLUMN_FAMILY = "longer";
	public static String HBASE_TM_WORD_LONGER_TABLE_PREFIX = "tm_word_longer_";

	public static String HBASE_TM_WORD_INVALID_COLUMN_FAMILY = "invalid";
	public static String HBASE_TM_WORD_INVALID_TABLE_PREFIX = "tm_word_invalid_";

}
