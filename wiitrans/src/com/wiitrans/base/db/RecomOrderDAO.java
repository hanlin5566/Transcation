package com.wiitrans.base.db;

import java.util.List;

import org.json.JSONObject;

import com.wiitrans.base.db.model.RecomFileBean;
import com.wiitrans.base.db.model.RecomFileBeanMapper;
import com.wiitrans.base.db.model.RecomOrderBean;
import com.wiitrans.base.db.model.RecomOrderBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class RecomOrderDAO extends CommonDAO {

	private RecomOrderBeanMapper _recomordermapper = null;
	private RecomFileBeanMapper _recomfilemapper = null;

	public int Init(Boolean loadConf, JSONObject obj) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf, obj);

			if (ret == Const.SUCCESS) {
				_recomordermapper = _session
						.getMapper(RecomOrderBeanMapper.class);
				_recomfilemapper = _session
						.getMapper(RecomFileBeanMapper.class);

				if (_recomordermapper != null && _recomfilemapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_recomordermapper = _session
						.getMapper(RecomOrderBeanMapper.class);
				_recomfilemapper = _session
						.getMapper(RecomFileBeanMapper.class);

				if (_recomordermapper != null && _recomfilemapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public RecomOrderBean Select(String code) {
		return _recomordermapper.Select(code);
	}

	public String SelectRecomT(int order_id) {
		return _recomordermapper.SelectRecomT(order_id);
	}

	public String SelectRecomE(int order_id) {
		return _recomordermapper.SelectRecomE(order_id);
	}

	public String SelectTeam(int customer_id, int pair_id) {
		return _recomordermapper.SelectTeam(customer_id, pair_id);
	}

	public List<RecomFileBean> SelectFiles(int order_id) {
		return _recomfilemapper.SelectFiles(order_id);
	}

	public RecomFileBean SelectFile(int file_id) {
		return _recomfilemapper.Select(file_id);
	}

}
