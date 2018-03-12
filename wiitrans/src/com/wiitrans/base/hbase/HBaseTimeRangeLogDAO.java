package com.wiitrans.base.hbase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.wiitrans.base.log.Log4j;

public class HBaseTimeRangeLogDAO extends HBaseCommonDAO {
	// private NumberFormat _numberFormat8 = null;
	// private int _idate = 0;
	// private String _tablename = null;
	private SimpleDateFormat _sdfDay = null;
	private String _log_columnfamily;
	private String _log_tableprefix;
	private SimpleDateFormat _sdfsecond = null;

	@Override
	public int Init(Boolean loadConf) {
		int ret = super.Init(loadConf);

		if (_sdfDay == null) {
			_sdfDay = new SimpleDateFormat("yyyyMMdd");
		}
		if (_sdfsecond == null) {
			_sdfsecond = new SimpleDateFormat("yyyyMMddHHmmss");
		}

		return ret;
	}

	public void SETTableName(String log_columnfamily, String log_tableprefix) {
		this._log_columnfamily = log_columnfamily;
		this._log_tableprefix = log_tableprefix;
	}

	public void CreateTable(int itoday) {

		String tablename = _log_tableprefix + String.valueOf(itoday);
		this.createTable(tablename, false, this._log_columnfamily);
	}

	public void InsertLog(String json, String rowkey, Date now, int itoday) {
		if (json != null && rowkey != null && rowkey.length() > 0) {

			HashMap<String, String> cols = new HashMap<String, String>();

			cols.put("json", json);

			cols.put("time", _sdfsecond.format(now));

			HbaseRow row = new HbaseRow(this._log_columnfamily, rowkey, cols);
			String tablename = _log_tableprefix + String.valueOf(itoday);
			this.insert(tablename, row);
		}
	}

	public JSONObject SearchLogs(int uid, String startrowkey, String endrowkey,
			ArrayList<Integer> datelist, int pageindex, int pagerowcount) {
		JSONObject ret = new JSONObject();

		// 校验输入参数
		if (pagerowcount <= 5 || pagerowcount > 100) {
			pagerowcount = 15;
		}

		if (pageindex < 1) {
			pageindex = 1;
		}

		if (uid > 0) {
			List<HbaseRow> hbaseRows = null;
			List<HbaseRow> hbaseRowstemp = null;
			long time = 0;
			try {
				if (datelist.size() > 0) {
					hbaseRows = new ArrayList<HbaseRow>();
					String tablename;
					for (Integer idate : datelist) {

						tablename = _log_tableprefix + String.valueOf(idate);
						if (this.tableExists(tablename)) {

							hbaseRowstemp = SearchLogsDaily(tablename,
									startrowkey, endrowkey);
							if (hbaseRowstemp != null
									&& hbaseRowstemp.size() > 0) {

								time = _sdfDay.parse(String.valueOf(idate))
										.getTime();

								for (HbaseRow hbaseRow : hbaseRowstemp) {

									hbaseRow.setRowKey(String.valueOf((time + Integer
											.parseInt(hbaseRow.getRowKey()
													.substring(10, 18))) / 1000));
									hbaseRows.add(hbaseRow);
								}
							}
						}
					}

					List<HbaseRow> selectedrows = new ArrayList<HbaseRow>();

					int allrowcount = hbaseRows.size();
					int allpagecount;
					if (allrowcount > 0) {
						allpagecount = (allrowcount + pagerowcount - 1)
								/ pagerowcount;
						if (pageindex > allpagecount) {
							pageindex = allpagecount;
						}
						ret.put("allrowcount", allrowcount);

						int beginrowindex = (pageindex - 1) * pagerowcount + 1;
						int endrowindex = pageindex * pagerowcount;
						if (endrowindex > allrowcount) {
							endrowindex = allrowcount;
						}

						beginrowindex = allrowcount + 1 - beginrowindex;
						endrowindex = allrowcount + 1 - endrowindex;

						for (int i = beginrowindex; i >= endrowindex; --i) {
							selectedrows.add(hbaseRows.get(i - 1));
						}
					}

					ret.put("allrowcount", hbaseRows.size());

					if (selectedrows.size() > 0) {
						JSONObject ary = new JSONObject();
						JSONObject logjson;
						Map<String, String> cols;
						int i = 0;

						for (HbaseRow hbaseRow : selectedrows) {
							logjson = new JSONObject();
							logjson.put("rowkey", hbaseRow.getRowKey());
							cols = hbaseRow.getCols();
							if (cols != null) {
								// for (String key : set) {
								// logjson.put(key, cols.get(key));
								// }
								logjson.put("time", cols.get("time"));
								logjson.put("json", cols.get("json"));
							}
							ary.put(String.valueOf(++i), logjson);
						}
						ret.put("list", ary);
					}

				}
			} catch (Exception e) {
				Log4j.error(e.getMessage());
			}
		}
		return ret;
	}

	public List<HbaseRow> SearchLogsDaily(String tablename, String startrowkey,
			String endrowkey) {
		List<HbaseRow> hbaseRows = null;

		HashMap<String, String> logcols = new HashMap<String, String>();
		logcols.put("time", "");
		logcols.put("json", "");
		HbaseRow logrow = new HbaseRow(this._log_columnfamily,
				String.valueOf(System.currentTimeMillis()), logcols);

		hbaseRows = this.findRange(tablename, startrowkey, endrowkey, logrow,
				true);

		return hbaseRows;
	}
}
