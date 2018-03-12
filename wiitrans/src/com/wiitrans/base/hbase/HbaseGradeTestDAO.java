package com.wiitrans.base.hbase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.misc.Util;

public class HbaseGradeTestDAO extends HBaseCommonDAO {
	public void CreateTable() {

		this.createTable(HbaseConfig.HBASE_GRADE_TEST_LOG_TABLE, false,
				HbaseConfig.HBASE_GRADE_TEST_LOG_COLUMN_FAMILY);

	}

	public void InsertLog(String translator_id, HashMap<String, String> map) {
		NumberFormat numberFormat8 = NumberFormat.getInstance();
		numberFormat8.setGroupingUsed(false);
		numberFormat8.setMaximumIntegerDigits(8);
		numberFormat8.setMinimumIntegerDigits(8);

		String timestamp = String.valueOf(System.currentTimeMillis());

		String len8uid = numberFormat8.format(Util.String2Int(translator_id));

		String rowkey = HBaseUtil.GetHash2FromString(len8uid) + len8uid
				+ timestamp;

		HashMap<String, String> cols = new HashMap<String, String>();
		Set<String> set = map.keySet();
		for (String key : set) {
			cols.put(key, map.get(key));
		}
		// cols.put("timestamp", timestamp);

		HbaseRow row = new HbaseRow(
				HbaseConfig.HBASE_GRADE_TEST_LOG_COLUMN_FAMILY, rowkey, cols);

		this.insert(HbaseConfig.HBASE_GRADE_TEST_LOG_TABLE, row);
	}

	public JSONObject SearchLogs(String uid, Set<String> set) {
		JSONObject ret = new JSONObject();
		// int pageindex, int pagerowcount,
		//
		// JSONObject ret = new JSONObject();
		// // 校验输入参数
		// if (pagerowcount <= 5 || pagerowcount > 100) {
		// pagerowcount = 15;
		// }
		//
		// if (pageindex < 1) {
		// pageindex = 1;
		// }

		NumberFormat numberFormat8 = NumberFormat.getInstance();
		numberFormat8.setGroupingUsed(false);
		numberFormat8.setMaximumIntegerDigits(8);
		numberFormat8.setMinimumIntegerDigits(8);

		String len8uid = numberFormat8.format(Util.String2Int(uid));

		String startrowkey = HBaseUtil.GetHash2FromString(len8uid) + len8uid
				+ "0000000000000";
		String endrowkey = HBaseUtil.GetHash2FromString(len8uid) + len8uid
				+ "9999999999999";

		List<HbaseRow> hbaseRows = null;
		List<HbaseRow> selectedrows = new ArrayList<HbaseRow>();
		int allrowcount;

		HashMap<String, String> logcols = new HashMap<String, String>();
		for (String key : set) {
			logcols.put(key, "");
		}
		HbaseRow logrow = new HbaseRow(
				HbaseConfig.HBASE_GRADE_TEST_LOG_COLUMN_FAMILY,
				String.valueOf(System.currentTimeMillis()), logcols);

		hbaseRows = this.findRange(HbaseConfig.HBASE_GRADE_TEST_LOG_TABLE,
				startrowkey, endrowkey, logrow, true);

		allrowcount = hbaseRows.size();
		if (allrowcount > 0) {

			for (int i = hbaseRows.size() - 1; i >= 0; --i) {
				selectedrows.add(hbaseRows.get(i));
			}

		}
		if (selectedrows.size() > 0) {
			JSONObject ary = new JSONObject();
			JSONObject logjson;
			Map<String, String> cols;
			int i = 0;

			for (HbaseRow hbaseRow : selectedrows) {
				logjson = new JSONObject();
				logjson.put("create_time",
						String.valueOf(Util.String2Long(hbaseRow.getRowKey()
								.substring(10, 18)) / 1000));
				logjson.put("rowkey", hbaseRow.getRowKey());
				cols = hbaseRow.getCols();
				if (cols != null) {
					for (String key : set) {
						logjson.put(key, cols.get(key));
					}
				}
				ary.put(String.valueOf(++i), logjson);
			}
			ret.put("list", ary);
		}
		return ret;
	}

	public JSONObject SearchLog(Set<String> set, String rowkey) {

		JSONObject ret = new JSONObject();

		HashMap<String, String> logcols = new HashMap<String, String>();
		for (String key : set) {
			logcols.put(key, "");
		}

		HbaseRow logrow = new HbaseRow(
				HbaseConfig.HBASE_GRADE_TEST_LOG_COLUMN_FAMILY,
				String.valueOf(rowkey), logcols);
		this.find(HbaseConfig.HBASE_GRADE_TEST_LOG_TABLE, logrow);

		ret.put("create_time",
				String.valueOf(Util.String2Long(logrow.getRowKey().substring(
						10, 18)) / 1000));
		ret.put("rowkey", logrow.getRowKey());
		Map<String, String> cols = logrow.getCols();
		if (cols != null) {
			for (String key : set) {
				ret.put(key, cols.get(key));
			}
		}

		return ret;

	}
}
