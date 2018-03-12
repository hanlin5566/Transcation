package com.wiitrans.base.hbase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import com.wiitrans.base.tm.TMTU;

public class HbaseTMTUDAO extends HBaseCommonDAO {
	private NumberFormat _numberFormat8 = null;
	private NumberFormat _numberFormat10 = null;
	private NumberFormat _numberFormat19 = null;
	private String _tm_text_columnfamily = null;
	private String _tm_text_table = null;
	private String _tm_tu_word_index_columnfamily = null;
	private String _tm_tu_word_index_table = null;
	private String _tm_word_tu_index_columnfamily = null;
	private String _tm_word_tu_index_table = null;
	// private String _tm_word_time_columnfamily = null;
	// private String _tm_word_time_table = null;
	private String _tm_word_longer_columnfamily = null;
	private String _tm_word_longer_table = null;

	// private String _tm_word_invalid_columnfamily = null;
	// private String _tm_word_invalid_table = null;

	@Override
	public int Init(Boolean loadConf) {
		int ret = super.Init(loadConf);
		if (_numberFormat8 == null) {
			_numberFormat8 = NumberFormat.getInstance();
			_numberFormat8.setGroupingUsed(false);
			_numberFormat8.setMaximumIntegerDigits(8);
			_numberFormat8.setMinimumIntegerDigits(8);
		}
		if (_numberFormat10 == null) {
			_numberFormat10 = NumberFormat.getInstance();
			_numberFormat10.setGroupingUsed(false);
			_numberFormat10.setMaximumIntegerDigits(10);
			_numberFormat10.setMinimumIntegerDigits(10);
		}
		if (_numberFormat19 == null) {
			_numberFormat19 = NumberFormat.getInstance();
			_numberFormat19.setGroupingUsed(false);
			_numberFormat19.setMaximumIntegerDigits(19);
			_numberFormat19.setMinimumIntegerDigits(19);
		}

		return ret;
	}

	public void SETTable(int tmid, boolean afresh) {
		this._tm_text_columnfamily = HbaseConfig.HBASE_TM_TEXT_COLUMN_FAMILY;
		this._tm_text_table = HbaseConfig.HBASE_TM_TEXT_TABLE_PREFIX
				+ _numberFormat10.format(tmid);

		this.createTable(this._tm_text_table, afresh,
				this._tm_text_columnfamily);

		this._tm_tu_word_index_columnfamily = HbaseConfig.HBASE_TM_TU_WORD_INDEX_COLUMN_FAMILY;
		this._tm_tu_word_index_table = HbaseConfig.HBASE_TM_TU_WORD_INDEX_TABLE_PREFIX
				+ _numberFormat10.format(tmid);

		this.createTable(this._tm_tu_word_index_table, afresh,
				this._tm_tu_word_index_columnfamily);

		this._tm_word_tu_index_columnfamily = HbaseConfig.HBASE_TM_WORD_TU_INDEX_COLUMN_FAMILY;
		this._tm_word_tu_index_table = HbaseConfig.HBASE_TM_WORD_TU_INDEX_TABLE_PREFIX
				+ _numberFormat10.format(tmid);

		this.createTable(this._tm_word_tu_index_table, afresh,
				this._tm_word_tu_index_columnfamily);

		// this._tm_word_time_columnfamily =
		// HbaseConfig.HBASE_TM_WORD_TIME_COLUMN_FAMILY;
		// this._tm_word_time_table =
		// HbaseConfig.HBASE_TM_WORD_TIME_TABLE_PREFIX
		// + _numberFormat8.format(tmid);
		//
		// this.createTable(this._tm_word_time_table, afresh,
		// this._tm_word_time_columnfamily);

		this._tm_word_longer_columnfamily = HbaseConfig.HBASE_TM_WORD_LONGER_COLUMN_FAMILY;
		this._tm_word_longer_table = HbaseConfig.HBASE_TM_WORD_LONGER_TABLE_PREFIX
				+ _numberFormat10.format(tmid);

		this.createTable(this._tm_word_longer_table, afresh,
				this._tm_word_longer_columnfamily);

		// this._tm_word_invalid_columnfamily =
		// HbaseConfig.HBASE_TM_WORD_INVALID_COLUMN_FAMILY;
		// this._tm_word_invalid_table =
		// HbaseConfig.HBASE_TM_WORD_INVALID_TABLE_PREFIX
		// + _numberFormat8.format(tmid);
		//
		// this.createTable(this._tm_word_invalid_table, afresh,
		// this._tm_word_invalid_columnfamily);

	}

	public void InsertTMText(TMTU tu) {
		// rowkey为8位tu编号
		String rowkey = _numberFormat8.format(tu._tuid);

		HashMap<String, String> cols = new HashMap<String, String>();

		cols.put("source", tu._tuv1);
		cols.put("target", tu._tuv2);

		HbaseRow row = new HbaseRow(this._tm_text_columnfamily, rowkey, cols);

		this.insert(this._tm_text_table, row);
	}

	public void InsertTMTexts(ArrayList<TMTU> list) {
		// rowkey为8位tu编号
		String rowkey;
		List<HbaseRow> rows;
		HashMap<String, String> cols;
		if (list != null && list.size() > 0) {
			rows = new ArrayList<HbaseRow>();

			for (TMTU tmtu : list) {
				rowkey = _numberFormat8.format(tmtu._tuid);
				cols = new HashMap<String, String>();
				cols.put("source", tmtu._tuv1);
				cols.put("target", tmtu._tuv2);
				rows.add(new HbaseRow(this._tm_text_columnfamily, rowkey, cols));
			}

			this.insert(this._tm_text_table, rows);
		}
	}

	public void InsertTUIndex(HashMap<Integer, long[]> map) {
		// rowkey为8位编号
		String rowkey;
		List<HbaseRow> rows;
		HashMap<String, String> cols;
		if (map != null) {
			rows = new ArrayList<HbaseRow>();

			Set<Integer> set = map.keySet();
			StringBuffer sb;
			long[] wordIDary;
			for (Integer tuID : set) {
				rowkey = _numberFormat8.format(tuID);
				sb = new StringBuffer();
				cols = new HashMap<String, String>();
				if (map.containsKey(tuID)) {
					wordIDary = map.get(tuID);
					if (wordIDary != null && wordIDary.length > 0) {
						for (long wordID : wordIDary) {
							sb.append(wordID).append(",");
						}
						sb.deleteCharAt(sb.length() - 1);
					}
				}
				cols.put("wordids", sb.toString());
				rows.add(new HbaseRow(this._tm_tu_word_index_columnfamily,
						rowkey, cols));
			}

			this.insert(this._tm_tu_word_index_table, rows);
		}
	}

	public void InsertWordIndex(HashMap<Long, int[]> map) {
		// rowkey为19位编号
		String rowkey;
		List<HbaseRow> rows;
		HashMap<String, String> cols;
		if (map != null) {
			rows = new ArrayList<HbaseRow>();

			Set<Long> set = map.keySet();
			StringBuffer sb;
			int[] tuIDary;
			for (Long wordID : set) {
				rowkey = _numberFormat19.format(wordID);
				sb = new StringBuffer();
				cols = new HashMap<String, String>();
				if (map.containsKey(wordID)) {
					tuIDary = map.get(wordID);
					if (tuIDary != null && tuIDary.length > 0) {
						for (int tuID : tuIDary) {
							sb.append(tuID).append(",");
						}
						sb.deleteCharAt(sb.length() - 1);
					}
				}
				cols.put("tuids", sb.toString());
				rows.add(new HbaseRow(this._tm_word_tu_index_columnfamily,
						rowkey, cols));
			}

			this.insert(this._tm_word_tu_index_table, rows);
		}
	}

	public void InsertWordLonger(HashMap<String, Long> map) {
		String rowkey;
		List<HbaseRow> rows;
		HashMap<String, String> cols;
		if (map != null) {
			rows = new ArrayList<HbaseRow>();
			Set<String> set = map.keySet();
			long wordID;
			for (String word : set) {
				rowkey = word;
				cols = new HashMap<String, String>();
				if (map.containsKey(word)) {
					wordID = map.get(word);
					cols.put("wordid", String.valueOf(wordID));
					rows.add(new HbaseRow(this._tm_word_longer_columnfamily,
							rowkey, cols));
				}

			}

			this.insert(this._tm_word_longer_table, rows);
		}
	}

	public JSONObject[] SearchTM(int[] tmids) {
		JSONObject[] result = new JSONObject[tmids.length];
		String rowkey;
		HashMap<String, String> cols = new HashMap<String, String>();
		cols.put("source", "");
		cols.put("target", "");
		HbaseRow row;
		for (int i = 0; i < tmids.length; i++) {
			rowkey = _numberFormat8.format(tmids[i]);
			row = new HbaseRow(this._tm_text_columnfamily, rowkey, cols);
			this.find(this._tm_text_table, row);
			result[i] = new JSONObject();
			result[i].put("source", row.getCols().get("source"));
			result[i].put("target", row.getCols().get("target"));
		}
		return result;
	}

	public List<HbaseRow> SearchTUIndex() {
		// String firstrowkey = _numberFormat8.format(0);
		// String lastrowkey = _numberFormat8.format(99999999);
		HbaseRow row;
		HashMap<String, String> cols = new HashMap<String, String>();
		cols.put("wordids", "");
		row = new HbaseRow(this._tm_tu_word_index_columnfamily, "", cols);
		return this.findRange(this._tm_tu_word_index_table, null, null, row,
				true);
	}

	public List<HbaseRow> SearchWordIndex() {
		// String firstrowkey = _numberFormat8.format(0);
		// String lastrowkey = _numberFormat8.format(99999999);
		HbaseRow row;
		HashMap<String, String> cols = new HashMap<String, String>();
		cols.put("tuids", "");
		row = new HbaseRow(this._tm_word_tu_index_columnfamily, "", cols);
		return this.findRange(this._tm_word_tu_index_table, null, null, row,
				true);
	}

	public List<HbaseRow> SearchWordLonger() {
		// String firstrowkey = _numberFormat8.format(0);
		// String lastrowkey = _numberFormat8.format(99999999);
		HbaseRow row;
		HashMap<String, String> cols = new HashMap<String, String>();
		cols.put("wordid", "");
		row = new HbaseRow(this._tm_word_longer_columnfamily, "", cols);
		return this
				.findRange(this._tm_word_longer_table, null, null, row, true);
	}
}
