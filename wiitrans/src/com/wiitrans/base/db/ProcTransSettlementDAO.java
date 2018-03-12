package com.wiitrans.base.db;

import java.util.HashMap;

import com.wiitrans.base.db.model.ProcTransSettlementMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ProcTransSettlementDAO extends CommonDAO {

	private ProcTransSettlementMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(ProcTransSettlementMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	// public int Settlement(int p_node_id, String p_code, int o_error) {
	// _mapper.Settlement(p_node_id, p_code, o_error);
	// return o_error;
	// }
	public void Settlement(HashMap<String, Object> map) {
		_mapper.Settlement(map);
	}
}
