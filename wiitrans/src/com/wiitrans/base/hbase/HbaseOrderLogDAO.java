package com.wiitrans.base.hbase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

public class HbaseOrderLogDAO extends HBaseCommonDAO {
	private String _log_columnfamily = null;
	private String _log_tableprefix = null;

	public void SETTableName(String log_columnfamily, String log_tableprefix) {
		this._log_columnfamily = log_columnfamily;
		this._log_tableprefix = log_tableprefix;
	}

	public void CreateTables() {
		String key;
		for (int i = 0; i <= 9; i++) {
			key = String.valueOf(i);
			this.createTable(HbaseConfig.HBASE_ORDER_LOG_TABLE_PREFIX + key,
					false, HbaseConfig.HBASE_ORDER_LOG_COLUMN_FAMILY);
		}
		for (int i = 0; i < 26; i++) {
			key = String.valueOf((char) ('a' + i));
			this.createTable(HbaseConfig.HBASE_ORDER_LOG_TABLE_PREFIX + key,
					false, HbaseConfig.HBASE_ORDER_LOG_COLUMN_FAMILY);
		}
	}

	public void InsertLog(String json, String ordercode) {
		if (ordercode != null && json != null && ordercode.length() > 0) {
			long timestamp = System.currentTimeMillis();

			// String rowkey = ordercode + String.valueOf(timestamp %
			// 864000000);
			String rowkey = ordercode + String.valueOf(timestamp);
			HashMap<String, String> cols = new HashMap<String, String>();

			cols.put("json", json);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			cols.put("time", sdf.format(new Date(timestamp)));

			HbaseRow row = new HbaseRow(this._log_columnfamily, rowkey, cols);

			this.insert(
					this._log_tableprefix
							+ ordercode.substring(ordercode.length() - 1)
									.toLowerCase(), row);
		}
	}

	public JSONObject SearchLogs(String ordercode) {
		JSONObject ret = new JSONObject();

		if (ordercode != null && ordercode.length() > 0) {
			JSONObject list = new JSONObject();
			String startrowkey = ordercode + "0000000000000";
			String endrowkey = ordercode + "9999999999999";

			HashMap<String, String> logcols = new HashMap<String, String>();
			logcols.put("time", "");
			logcols.put("json", "");
			HbaseRow logrow = new HbaseRow(this._log_columnfamily,
					String.valueOf(System.currentTimeMillis()), logcols);

			List<HbaseRow> hbaseRows = this.findRange(
					this._log_tableprefix
							+ ordercode.substring(ordercode.length() - 1)
									.toLowerCase(), startrowkey, endrowkey,
					logrow, true);
			for (HbaseRow hbaseRow : hbaseRows) {
				list.put(hbaseRow.getRowKey().substring(8, 18), hbaseRow
						.getCols().get("json"));
			}
			ret.put("list", list);
		}
		return ret;
	}
}
