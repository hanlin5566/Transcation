package com.wiitrans.automation.logic;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wiitrans.base.db.GenericSQLDAO;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.hbase.HbaseDAO;
import com.wiitrans.base.log.Log4j;


public class SyncRoomLogicImpl implements Logic{
	/**
	 * @param jsonObject
	 * mailTemplate:对应mail.xml中 邮件模板的key
	 * 其他参数对应 mail.xml中 #{uid+" 此种标识的值
	 */
	@Override
	public void invoke(JSONObject jsonObject) throws Exception{
		//同步房间信息
		Log4j.log("start invoke SyncRoomLogicImpl");
		JSONObject paramJson = jsonObject.getJSONObject("param");
		String order_id = (String) paramJson.get("order_id");
		String room_id = (String) paramJson.get("room_id");
		String create_time = (String) paramJson.get("create_time");
		String name = (String) paramJson.get("name");
		String node_id = (String) paramJson.get("node_id");
		String SQL = "INSERT INTO `node_room` (`room_id`,`order_id`,`name`,`create_time`,`node_id`) VALUES ('"+room_id+"','"+order_id+"','"+name+"','"+create_time+"','"+node_id+"');";
		GenericSQLDAO sqlDAO = new GenericSQLDAO();
		sqlDAO.Init(true);
		sqlDAO.executeSQL(SQL);
		sqlDAO.Commit();
		sqlDAO.UnInit();
		//创建聊天记录表
		HbaseDAO hbaseDAO = new HbaseDAO();
		hbaseDAO.Init(true);
		hbaseDAO.createTable(HbaseConfig.HBASE_TABLE_PREFIX_ROOM + room_id,
				false, HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT);
		hbaseDAO.UnInit();
	}
}
