package com.wiitrans.base.hbase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.hbase.model.LogStat;
import com.wiitrans.base.misc.Util;

public class HBaseTimeLogDAO extends HBaseCommonDAO {
	private NumberFormat _numberFormat5 = null;
	private String _log_index = null;
	private String _log_columnfamily = null;
	private String _log_tableprefix = null;

	@Override
	public int Init(Boolean loadConf) {
		int ret = super.Init(loadConf);
		if (_numberFormat5 == null) {
			_numberFormat5 = NumberFormat.getInstance();
			_numberFormat5.setGroupingUsed(false);
			_numberFormat5.setMaximumIntegerDigits(5);
			_numberFormat5.setMinimumIntegerDigits(5);
		}

		return ret;
	}

	public void SETTableName(String log_index, String log_columnfamily,
			String log_tableprefix) {
		this._log_index = log_index;
		this._log_columnfamily = log_columnfamily;
		this._log_tableprefix = log_tableprefix;
	}

	public JSONObject SearchLog(Set<String> set, String rowkey, int table_id) {

		JSONObject ret = new JSONObject();

		HashMap<String, String> logcols = new HashMap<String, String>();
		for (String key : set) {
			logcols.put(key, "");
		}
		logcols.put("tid", "");

		HbaseRow logrow = new HbaseRow(this._log_columnfamily,
				String.valueOf(rowkey), logcols);
		this.find(this._log_tableprefix + _numberFormat5.format(table_id),
				logrow);

		ret.put("create_time",
				String.valueOf(Util.String2Long(logrow.getRowKey()) / 1000));
		ret.put("rowkey", logrow.getRowKey());
		Map<String, String> cols = logrow.getCols();
		if (cols != null) {
			for (String key : set) {
				ret.put(key, cols.get(key));
			}
			ret.put("tid", cols.get("tid"));
		}

		return ret;

	}

	public void InsertLog(HashMap<String, String> map, int table_id,
			int rowcount) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		if (rowcount >= 1000 && table_id >= 1) {
			// 更新索引表中table_id行的结束时间
			HashMap<String, String> endcols = new HashMap<String, String>();
			endcols.put("endtime", timestamp);

			HbaseRow row = new HbaseRow(this._log_columnfamily,
					_numberFormat5.format(table_id), endcols);

			this.insert(this._log_index, row);
			// 创建新表
			++table_id;
			this.createTable(
					this._log_tableprefix + _numberFormat5.format(table_id),
					false, this._log_columnfamily);

			// 更新索引表中table_id（已经所加一之后的值）行的开始时间
			HashMap<String, String> begincols = new HashMap<String, String>();
			begincols.put("begintime", timestamp);

			row = new HbaseRow(this._log_columnfamily,
					_numberFormat5.format(table_id), begincols);

			this.insert(this._log_index, row);

		} else if (rowcount <= 0 && table_id == 1) {
			// 创建索引表
			this.createTable(this._log_index, false, this._log_columnfamily);
			// 创建表
			this.createTable(
					this._log_tableprefix + _numberFormat5.format(table_id),
					false, this._log_columnfamily);

			// 更新索引表中table_id行的开始时间
			HashMap<String, String> begincols = new HashMap<String, String>();
			begincols.put("begintime", timestamp);

			HbaseRow row = new HbaseRow(this._log_columnfamily,
					_numberFormat5.format(table_id), begincols);

			this.insert(this._log_index, row);

		}

		HashMap<String, String> cols = new HashMap<String, String>();
		Set<String> set = map.keySet();
		for (String key : set) {
			cols.put(key, map.get(key));
		}
		cols.put("timestamp", timestamp);
		cols.put("tid", _numberFormat5.format(table_id));
		HbaseRow row = new HbaseRow(this._log_columnfamily, timestamp, cols);

		this.insert(this._log_tableprefix + _numberFormat5.format(table_id),
				row);
	}

	public LogStat GetLogStat() {

		// 创建索引表
		this.createTable(this._log_index, false, this._log_columnfamily);

		LogStat logstat = null;

		HashMap<String, String> cols = new HashMap<String, String>();
		cols.put("begintime", "");
		cols.put("endtime", "");
		HbaseRow row = new HbaseRow(this._log_columnfamily,
				String.valueOf(System.currentTimeMillis()), cols);

		List<HbaseRow> hbaseRows = this.findRange(this._log_index, null, null,
				row, true);
		if (hbaseRows != null && hbaseRows.size() > 0) {
			logstat = new LogStat();
			HbaseRow hbaseRow = hbaseRows.get(hbaseRows.size() - 1);
			logstat.table_id = Util.String2Int(hbaseRow.getRowKey());

			HashMap<String, String> logcols = new HashMap<String, String>();
			logcols.put("tid", "");

			HbaseRow logrow = new HbaseRow(this._log_columnfamily,
					String.valueOf(System.currentTimeMillis()), logcols);
			List<HbaseRow> loghbaseRows = this.findRange(this._log_tableprefix
					+ _numberFormat5.format(logstat.table_id), null, null,
					logrow, true);
			if (loghbaseRows != null && loghbaseRows.size() > 0) {
				logstat.rowcount = loghbaseRows.size();
				logstat.startrowkey = Util.String2Long(loghbaseRows.get(0)
						.getRowKey());
				logstat.endrowkey = Util.String2Long(loghbaseRows.get(
						logstat.rowcount - 1).getRowKey());
				logstat.allrowcount = logstat.rowcount + 1000
						* (hbaseRows.size() - 1);
			}
		}

		return logstat;
	}

	public JSONObject SearchLogs(int pageindex, int pagerowcount, int begin,
			int end, Set<String> set) {

		JSONObject ret = new JSONObject();
		// 校验输入参数
		if (pagerowcount <= 5 || pagerowcount > 100) {
			pagerowcount = 15;
		}

		if (pageindex < 1) {
			pageindex = 1;
		}

		if (begin > end || begin <= 0 || end <= 0) {
			begin = 0;
			end = 0;
		}

		// 输入参数换算成毫秒
		long begintime = begin * 1000L;
		long endtime = end * 1000L;

		HashMap<String, String> indexcols = new HashMap<String, String>();
		indexcols.put("begintime", "");
		indexcols.put("endtime", "");
		HbaseRow row = new HbaseRow(this._log_columnfamily,
				String.valueOf(System.currentTimeMillis()), indexcols);

		List<HbaseRow> hbaseRows = this.findRange(this._log_index, null, null,
				row, true);

		if (hbaseRows != null && hbaseRows.size() > 0) {
			int first_table_id = 0;
			int last_table_id = 0;
			// long startrowkey;
			// long endrowkey;
			long startrowkeytmp;
			long endrowkeytmp;
			if (begintime == 0L || endtime == 0L) {
				first_table_id = Util.String2Int(hbaseRows.get(0).getRowKey());
				begintime = Util.String2Long(hbaseRows.get(0).getCols()
						.get("begintime"));
				last_table_id = Util.String2Int(hbaseRows.get(
						hbaseRows.size() - 1).getRowKey());
				endtime = System.currentTimeMillis();

			} else {
				for (HbaseRow hbaseRow : hbaseRows) {
					Map<String, String> retCols = hbaseRow.getCols();
					startrowkeytmp = Util.String2Long(retCols.get("begintime"));
					if (first_table_id == 0) {
						first_table_id = Util.String2Int(hbaseRow.getRowKey());
					} else {
						if (startrowkeytmp > 0L && begintime >= startrowkeytmp) {
							first_table_id = Util.String2Int(hbaseRow
									.getRowKey());
						}
					}
					endrowkeytmp = Util.String2Long(retCols.get("endtime")
							.toString());
					if (last_table_id == 0) {
						if (endrowkeytmp > 0L) {
							if (endrowkeytmp >= endtime) {
								last_table_id = Util.String2Int(hbaseRow
										.getRowKey());
							}
						} else {
							last_table_id = Util.String2Int(hbaseRow
									.getRowKey());
						}
					}
				}
			}
			// System.out.println(first_table_id);
			// System.out.println(last_table_id);
			// System.out.println(begintime);
			// System.out.println(endtime);

			List<HbaseRow> firsthbaseRows;
			List<HbaseRow> lasthbaseRows;
			List<HbaseRow> selectedrows = new ArrayList<HbaseRow>();
			int allrowcount;
			int allpagecount;

			HashMap<String, String> logcols = new HashMap<String, String>();
			for (String key : set) {
				logcols.put(key, "");
			}
			logcols.put("tid", "");
			// orderlogcols.put("uid", "");
			// orderlogcols.put("order_id", "");
			// orderlogcols.put("opera", "");
			// orderlogcols.put("ip", "");
			// orderlogcols.put("content", "");
			// orderlogcols.put("user_name", "");
			HbaseRow logrow = new HbaseRow(this._log_columnfamily,
					String.valueOf(System.currentTimeMillis()), logcols);

			if (first_table_id > 0 && last_table_id > 0
					&& first_table_id <= last_table_id) {

				if (first_table_id == last_table_id) {

					firsthbaseRows = this.findRange(this._log_tableprefix
							+ _numberFormat5.format(first_table_id),
							String.valueOf(begintime), String.valueOf(endtime),
							logrow, true);

					allrowcount = firsthbaseRows.size();
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
							selectedrows.add(firsthbaseRows.get(i - 1));
						}
					}
				} else {

					firsthbaseRows = this.findRange(this._log_tableprefix
							+ _numberFormat5.format(first_table_id),
							String.valueOf(begintime), String.valueOf(endtime),
							logrow, true);

					lasthbaseRows = this.findRange(this._log_tableprefix
							+ _numberFormat5.format(last_table_id),
							String.valueOf(begintime), String.valueOf(endtime),
							logrow, true);
					allrowcount = firsthbaseRows.size() + lasthbaseRows.size()
							+ (last_table_id - first_table_id - 1) * 1000;
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

						int last_table_row_count = lasthbaseRows.size();
						int begin_table_offset = (beginrowindex
								- last_table_row_count + 999) / 1000;
						int end_table_offset = (endrowindex
								- last_table_row_count + 999) / 1000;

						int allrowcounttmp = last_table_row_count
								+ end_table_offset * 1000;

						List<HbaseRow> beginhbaseRows = this.findRange(
								this._log_tableprefix
										+ _numberFormat5.format(last_table_id
												- begin_table_offset),
								String.valueOf(begintime),
								String.valueOf(endtime), logrow, true);
						if (begin_table_offset != end_table_offset) {
							// 1000条
							List<HbaseRow> endhbaseRows = this
									.findRange(
											this._log_tableprefix
													+ _numberFormat5
															.format(last_table_id
																	- end_table_offset),
											null, null, logrow, true);
							endhbaseRows.addAll(beginhbaseRows);
							beginhbaseRows = endhbaseRows;

						}

						beginrowindex = allrowcounttmp + 1 - beginrowindex;
						endrowindex = allrowcounttmp + 1 - endrowindex;
						for (int i = beginrowindex; i >= endrowindex; --i) {
							selectedrows.add(beginhbaseRows.get(i - 1));
						}
					}
				}

			}
			if (selectedrows.size() > 0) {
				JSONObject ary = new JSONObject();
				JSONObject logjson;
				Map<String, String> cols;
				int i = 0;

				for (HbaseRow hbaseRow : selectedrows) {
					logjson = new JSONObject();
					logjson.put("create_time", String.valueOf(Util
							.String2Long(hbaseRow.getRowKey()) / 1000));
					logjson.put("rowkey", hbaseRow.getRowKey());
					cols = hbaseRow.getCols();
					if (cols != null) {
						for (String key : set) {
							logjson.put(key, cols.get(key));
						}
						logjson.put("tid", cols.get("tid"));
						// orderlog.put("order_id", cols.get("order_id"));
						// orderlog.put("opera", cols.get("opera"));
						// orderlog.put("ip", cols.get("ip"));
						// orderlog.put("content", cols.get("content"));
						// orderlog.put("user_name", cols.get("user_name"));

					}
					ary.put(String.valueOf(++i), logjson);
				}
				ret.put("list", ary);
			}
		}

		return ret;

	}
}
