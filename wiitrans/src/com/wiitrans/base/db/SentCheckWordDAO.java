package com.wiitrans.base.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.model.SentCheckWordBean;
import com.wiitrans.base.db.model.SentCheckWordServiceMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.xml.WiitransConfig;

public class SentCheckWordDAO {
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

	private SentCheckWordServiceMapper _mapper = null;

	public int Init() {
		int ret = Const.FAIL;

		try {
			WiitransConfig.getInstance(1);

			File file = new File(BundleConf.BUNDLE_TMSVR_MYBATIS);
			InputStream inputStream = new FileInputStream(file);
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
					.build(inputStream);

			_session = sqlSessionFactory.openSession();
			_mapper = _session.getMapper(SentCheckWordServiceMapper.class);

			if (_mapper != null) {
				ret = Const.SUCCESS;
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public void ImportSentCheckWord(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", fileName);
		_mapper.ImportSentCheckWord(map);
	}

	public void DeleteSentCheckWord(int fid, int sent_id) {
		_mapper.DeleteSentCheckWord(fid, sent_id);
	}

	public void ImportUnknown(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", fileName);
		_mapper.ImportUnknown(map);
	}

	public ArrayList<SentCheckWordBean> Check(int fid, int sent_id) {
		return _mapper.Check(fid, sent_id);
	}

	public ArrayList<SentCheckWordBean> CheckVariation(int fid, int sent_id) {
		return _mapper.CheckVariation(fid, sent_id);
	}

	public ArrayList<SentCheckWordBean> CheckSymbol(int fid, int sent_id) {
		return _mapper.CheckSymbol(fid, sent_id);
	}

	public void InsertWord(String word) {
		_mapper.InsertWord(word);
	}

	public void InsertVariation(List<VariationCheckWord> list) {
		_mapper.InsertVariation(list);
	}
}
