package com.wiitrans.base.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.model.TMServiceIndexBean;
import com.wiitrans.base.db.model.TMServiceMapper;
import com.wiitrans.base.db.model.TMServiceTextBean;
import com.wiitrans.base.db.model.TMServiceTimesBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class TMServiceDAO {
	protected SqlSession _session = null;

	public int Commit() {
		int ret = Const.FAIL;

		_session.commit();
		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		try {

			if (_session != null) {
				_session.close();
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	private TMServiceMapper _mapper = null;
	private NumberFormat _numberFormat10 = null;

	public int Init() {
		int ret = Const.FAIL;

		try {

			WiitransConfig.getInstance(2);

			File file = new File(BundleConf.BUNDLE_TMSVR_MYBATIS);
			InputStream inputStream = new FileInputStream(file);
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
					.build(inputStream);

			_session = sqlSessionFactory.openSession();
			_mapper = _session.getMapper(TMServiceMapper.class);

			if (_mapper != null) {
				ret = Const.SUCCESS;
			}

			if (_numberFormat10 == null) {
				_numberFormat10 = NumberFormat.getInstance();
				_numberFormat10.setGroupingUsed(false);
				_numberFormat10.setMaximumIntegerDigits(10);
				_numberFormat10.setMinimumIntegerDigits(10);
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public void DropTMIndexIfExists(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_index");
		_mapper.DropTMIndexIfExists(map);
	}

	public void CreateTMIndex(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_index");
		_mapper.CreateTMIndex(map);
	}

	public void DropTMWordIfExists(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_word");
		_mapper.DropTMWordIfExists(map);
	}

	public void CreateTMWord(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_word");
		_mapper.CreateTMWord(map);
	}

	public void DropTMTimeIfExists(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_time");
		_mapper.DropTMTimeIfExists(map);
	}

	public void CreateTMTime(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_time");
		_mapper.CreateTMTime(map);
	}

	public void DropTMTextIfExists(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_text");
		_mapper.DropTMTextIfExists(map);
	}

	public void CreateTMText(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_text");
		_mapper.CreateTMText(map);
	}

	public void ImportIndex(int tmID, String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_index");
		map.put("fileName", fileName);
		_mapper.ImportIndex(map);
	}

	public void ImportText(int tmID, String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_text");
		map.put("fileName", fileName);
		_mapper.ImportText(map);
	}

	public void ImportTime(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("timesTableName", "tm" + _numberFormat10.format(tmID) + "_time");
		map.put("indexTableName", "tm" + _numberFormat10.format(tmID)
				+ "_index");
		_mapper.ImportTime(map);
	}

	public void ImportWord(int tmID, String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_word");
		map.put("fileName", fileName);
		_mapper.ImportWord(map);
	}

	public List<TMServiceTimesBean> SelectTimes(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_time");
		return _mapper.SelectTimes(map);
	}

	public List<TMServiceIndexBean> SelectIndexByWordIDs(int tmID,
			long[] wordIDs) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_index");
		map.put("wordIDs", wordIDs);
		return _mapper.SelectIndexByWordIDs(map);
	}

	public List<TMServiceIndexBean> SelectIndexByTuIDs(int tmID, int[] tuIDs) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_index");
		map.put("tuIDs", tuIDs);
		return _mapper.SelectIndexByTuIDs(map);
	}

	public List<TMServiceTextBean> SelectTextByTuIDs(int tmID, int[] tuIDs) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_text");
		map.put("tuIDs", tuIDs);
		return _mapper.SelectTextByTuIDs(map);
	}

	public List<TMServiceTextBean> SelectTextByCHNWordIDs(int tmID,
			long[] wordIDs) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tmid", tmID);
		StringBuffer sb = new StringBuffer();
		if (wordIDs != null && wordIDs.length > 0) {
			for (long i : wordIDs) {
				sb.append(',').append(i);
			}

			if (sb.length() > 0) {
				map.put("wordidsstr", sb.substring(1));
				return _mapper.SelectTextByCHNWordIDs(map);
			} else {
				return new ArrayList<TMServiceTextBean>();
			}
		}
		return new ArrayList<TMServiceTextBean>();
	}

	public int SelectTextMaxID(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tableName", "tm" + _numberFormat10.format(tmID) + "_text");
		return _mapper.SelectTextMaxID(map);
	}

	public void TruncateTime(int tmID) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("timesTableName", "tm" + _numberFormat10.format(tmID) + "_time");
		_mapper.TruncateTime(map);
	}

	public void CleanUpTM(int tmID, int langtype) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tmid", tmID);
		map.put("langtype", langtype);
		_mapper.CleanUpTM(map);
	}
}
