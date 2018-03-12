package com.wiitrans.base.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class GenericSQLDAO extends CommonDAO{
	
	private Connection _conn = null;
	@Override
	public int Init(Boolean loadConf) {
		// TODO Auto-generated method stub
		int ret = Const.FAIL;
		try {
			ret = super.Init(loadConf);
			_conn = _session.getConfiguration().getEnvironment().getDataSource().getConnection();
		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}
	
	public Map<String, Object> selectOne(String sql,Map<String, Object> param){
		Map<String, Object> retMap = new HashMap<String, Object>();
		try {
			if(_conn !=null){
				Statement st = _conn.createStatement();
				sql = parseSQL(sql, param);
				Log4j.info("select sql:  "+sql);
				ResultSet rs = st.executeQuery(sql);
				
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int colCount = rsMetaData.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= colCount; i++) {
						String colName = rsMetaData.getColumnLabel(i);
						retMap.put(colName, rs.getString(colName));
					}
					break;
				}
				rs.close();
				st.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
		}
		return retMap;
	}
	
	public List<Map<String, Object>> selectList(String sql,Map<String, Object> param){
		List<Map<String, Object>> retList = new ArrayList<Map<String,Object>>();
		try {
			if(_conn !=null){
				Statement st = _conn.createStatement();
				sql = parseSQL(sql, param);
				Log4j.info("select sql:  "+sql);
				ResultSet rs = st.executeQuery(sql);
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int colCount = rsMetaData.getColumnCount();
				while (rs.next()) {
					Map<String, Object> rowMap = new HashMap<String, Object>();
					for (int i = 1; i <= colCount; i++) {
						String colName = rsMetaData.getColumnLabel(i);
						rowMap.put(colName, rs.getString(colName));
					}
					retList.add(rowMap);
				}
				rs.close();
				st.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
		}
		return retList;
	}
	
	public static void main(String[] args) {
		String string  = "SELECT u.user_id,u.nickname,u.account,td.translator_id,td.#{test},td.experience,td.money,td.aggregate_money,td.word_count,td.normal_order_number,td.total_order_number,td.create_timeFROM user uLEFT OUTER JOIN translator_details td ON u.user_id = td.translator_idWHERE u.user_id=#{uid};";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("uid", "123");
		map.put("test", "xxxwww");
		GenericSQLDAO d = new GenericSQLDAO();
		String sql = d.parseSQL(string, map);
		System.out.println(sql);
	}
	
	public boolean executeSQL(String sql){
		boolean ret = false;
		try {
			if(_conn !=null){
				Log4j.info("execute sql:"+sql);
				Statement st = _conn.createStatement();
				st.execute(sql);
				st.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
		}
		return ret;
	}
	
	private String parseSQL(String sql,Map<String, Object> param){
		try {
			String openToken  = "#{";
			String closeToken = "}";
			String text = sql;
			 StringBuilder builder = new StringBuilder();
			    if (text != null && text.length() > 0) {
			      char[] src = text.toCharArray();
			      int offset = 0;
			      int start = text.indexOf(openToken, offset);
			      while (start > -1) {
			        if (start > 0 && src[start - 1] == '\\') {
			          // the variable is escaped. remove the backslash.
			          builder.append(src, offset, start - offset - 1).append(openToken);
			          offset = start + openToken.length();
			        } else {
			          int end = text.indexOf(closeToken, start);
			          if (end == -1) {
			            builder.append(src, offset, src.length - offset);
			            offset = src.length;
			          } else {
			            builder.append(src, offset, start - offset);
			            offset = start + openToken.length();
			            String content = new String(src, offset, end - offset);
			            builder.append(param.containsKey(content)?StringEscapeUtils.escapeSql(param.get(content).toString()):"");
			            offset = end + closeToken.length();
			          }
			        }
			        start = text.indexOf(openToken, offset);
			      }
			      if (offset < src.length) {
			        builder.append(src, offset, src.length - offset);
			      }
			    }
			    sql = builder.toString();		
		} catch (Exception e) {
			Log4j.error(" SQL:"+sql+" param:"+param+" case:"+e);
		}
		return sql;
	}
	
	@Override
	public int Commit() {
		// TODO Auto-generated method stub
		return super.Commit();
	}

	@Override
	public int UnInit(){
		// TODO Auto-generated method stub
		try {
			if (_conn != null) {
				_conn.close();
			}
		} catch (Exception ex) {
			Log4j.error(ex);
		}
		return super.UnInit();
	}
}
