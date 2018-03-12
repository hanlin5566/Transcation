package com.wiitrans.automation.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.GenericSQLDAO;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.misc.ZipUtil;
import com.wiitrans.base.sync.SyncData;

public class SyncDataLogicImpl implements Logic{
	/**
	 * @param jsonObject
	 * dataTemplate:对应sync-data.xml中 同步类型的key
	 * syncType:send:读取数据发送至其他节点，rec:执行同步数据SQL
	 * 其他参数对应 sync-data.xml中 #{uid} 此种标识的值
	 */
	@Override
	public void invoke(JSONObject jsonObject) throws Exception{
		JSONObject paramJson = jsonObject.getJSONObject("param");
		String syncType = paramJson.getString("syncType");//send rec
		Log4j.log("start invoke SyncDataLogicImpl");
		if(syncType.equals("send")){
			this.sendSyncTask(jsonObject);
		}else{
			this.syncData(jsonObject);
		}
	}
	/**
	 * @param jsonObject
	 * syncType:对应sync-data.xml中 同步类型的key
	 * 其他参数对应 sync-data.xml中 #{uid} 此种标识的值
	 */
	private void sendSyncTask(JSONObject jsonObject) throws Exception{
		JSONObject paramJson = jsonObject.getJSONObject("param");
		String dataTemplate = paramJson.getString("dataTemplate");//sync-data.xml name
		if(!BundleConf.SYNC_DATA_TEMPLATE.containsKey(dataTemplate)){
			//此处抛出的异常会记录到mysql的task表中
			Log4j.error("dataTemplate not found! dataTemplate:"+dataTemplate);
			throw new Exception("dataTemplate not found! dataTemplate:"+dataTemplate);
		}else{
			SyncData data = BundleConf.SYNC_DATA_TEMPLATE.get(dataTemplate);
			String targetURL = data.getTargetURL();
			String sourceSQL = data.getSourceSql();
			List<String> targetSQLs = data.getTargetSql();
			List<String> targetSQLs_new = new ArrayList<String>();
			GenericSQLDAO sqlDAO = new GenericSQLDAO();
			sqlDAO.Init(true);
			Map<String, Object> param = Util.convert(paramJson);
			//修改未支持多行的结果集
			List<Map<String, Object>> list = sqlDAO.selectList(sourceSQL, param);
			sqlDAO.UnInit();
			for (Map<String, Object> map : list) {
				//targetSQL会按顺序将每行数据执行一次
				for (String targetSQL : targetSQLs) {
					targetSQL = Util.parseContent(targetSQL, map,true);
					targetSQLs_new.add(targetSQL);
				}
			}
			String execSQL = new JSONArray(targetSQLs_new.toArray()).toString();
			paramJson.put("compress", execSQL.length()>5000);
			if(paramJson.getBoolean("compress")){
			    execSQL = ZipUtil.compress(execSQL);
			}
			paramJson.put("execSQL", execSQL);
			paramJson.put("syncType", "rec");//将类型由发送置未接收，发送至其他节点
			jsonObject.put("param", paramJson);
			jsonObject.remove("id");//移除ID，不沿用此连接，让其重新生成。
			Log4j.info("send:["+dataTemplate+"]  to:["+ targetURL+"] param:"+jsonObject.toString());
			new HttpSimulator(targetURL).executeMethodTimeOut(jsonObject.toString(),3);
		}
	}
	/**
	 * @param jsonObject
	 * 
	 */
	private void syncData(JSONObject jsonObject)throws Exception{
		GenericSQLDAO sqlDAO = new GenericSQLDAO();
		sqlDAO.Init(true);
		JSONObject param = (JSONObject) jsonObject.get("param");
		String execSQL =  param.get("execSQL").toString();
		if(param.getBoolean("compress")){
		    execSQL = ZipUtil.decompress(execSQL);
		}
		JSONArray sqls = new JSONArray(execSQL);
		for (int i = 0; i < sqls.length(); i++) {
			String sql = sqls.getString(i);
			sqlDAO.executeSQL(sql);
		}
		sqlDAO.Commit();
		sqlDAO.UnInit();
	}
}
