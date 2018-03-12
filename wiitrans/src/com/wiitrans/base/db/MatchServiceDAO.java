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
import com.wiitrans.base.bundle.BundleMatchVar;
import com.wiitrans.base.db.model.MatchServiceMapper;
import com.wiitrans.base.db.model.TransScoreBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.xml.WiitransConfig;

public class MatchServiceDAO {
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

	private MatchServiceMapper _mapper = null;

	public int Init() {
		int ret = Const.FAIL;

		try {
			WiitransConfig.getInstance(1);

			File file = new File(BundleConf.BUNDLE_TMSVR_MYBATIS);
			InputStream inputStream = new FileInputStream(file);
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
					.build(inputStream);

			_session = sqlSessionFactory.openSession();
			_mapper = _session.getMapper(MatchServiceMapper.class);

			if (_mapper != null) {
				ret = Const.SUCCESS;
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public void ImportOrderKeyword(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", fileName);
		_mapper.ImportOrderKeyword(map);
	}

	public void DeleteNewOrderKeyword(int order_id) {
		_mapper.DeleteNewOrderKeyword(order_id);
	}

	public void ImportNewOrderKeyword(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", fileName);
		_mapper.ImportNewOrderKeyword(map);
	}

	public void DeleteNoOrderKeyword(String java_id) {
		_mapper.DeleteNoOrderKeyword(java_id);
	}

	public void ImportNoOrderKeyword(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fileName", fileName);
		_mapper.ImportNoOrderKeyword(map);
	}

	public void AddKeywordScore(int order_id) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("order_id", order_id);
		_mapper.AddKeywordScore(map);
	}

	public List<TransScoreBean> SelectTopTrans(int order_id, int industry_id,
			int price_level_id, ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("order_id", order_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTopTrans(map);
	}

	public List<TransScoreBean> SelectTopEdits(int order_id, int industry_id,
			int price_level_id, ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("order_id", order_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTopEdits(map);
	}

	public List<TransScoreBean> SelectTrans(int order_id, int industry_id,
			int price_level_id, int[] translatorIDs,
			ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("order_id", order_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		map.put("translatorIDs", translatorIDs);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTrans(map);
	}

	public List<TransScoreBean> SelectTopTransByJavaID(String java_id,
			int industry_id, int price_level_id, ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("java_id", java_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTopTransByJavaID(map);
	}

	public List<TransScoreBean> SelectTopEditsByJavaID(String java_id,
			int industry_id, int price_level_id, ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("java_id", java_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTopEditsByJavaID(map);
	}

	public List<TransScoreBean> SelectTransByJavaID(String java_id,
			int industry_id, int price_level_id, int[] translatorIDs,
			ArrayList<BundleMatchVar> list) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("java_id", java_id);
		map.put("industry_id", industry_id);
		map.put("price_level_id", price_level_id);
		map.put("translatorIDs", translatorIDs);
		for (BundleMatchVar bundleMatchVar : list) {
			switch (bundleMatchVar.name) {
			case "experience":
				map.put("experience_a", bundleMatchVar.a);
				map.put("experience_b", bundleMatchVar.b);
				break;
			case "word_count":
				map.put("word_count_a", bundleMatchVar.a);
				map.put("word_count_b", bundleMatchVar.b);
				break;
			case "order_number":
				map.put("order_number_a", bundleMatchVar.a);
				map.put("order_number_b", bundleMatchVar.b);
				break;

			default:
				break;
			}
		}
		return _mapper.SelectTransByJavaID(map);
	}
}
