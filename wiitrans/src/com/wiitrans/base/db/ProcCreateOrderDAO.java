package com.wiitrans.base.db;

import java.util.HashMap;

import com.wiitrans.base.db.model.ProcCreateOrderMlvMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ProcCreateOrderDAO extends CommonDAO {
	private ProcCreateOrderMlvMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(ProcCreateOrderMlvMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public String CreateOrderMlv(int p_pair_id, int p_industry_id,
			int p_customer_id, int p_price_level_id, int p_translator_id,
			int p_editor_id, int p_word_count, int p_analyse_word_count,
			int p_currency_id, float p_total_money, int expected_delivery_time,
			int expected_delivery_time_t, int expected_delivery_time_e,
			boolean p_analyse, String p_description, String p_sql, int p_tm_id,
			int p_tg_id, String p_cat_ids, String p_name, String p_file_id) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("p_pair_id", p_pair_id);
		map.put("p_industry_id", p_industry_id);
		map.put("p_customer_id", p_customer_id);
		map.put("p_price_level_id", p_price_level_id);
		map.put("p_translator_id", p_translator_id);
		map.put("p_editor_id", p_editor_id);
		map.put("p_word_count", p_word_count);
		map.put("p_analyse_word_count", p_analyse_word_count);
		map.put("p_currency_id", p_currency_id);
		map.put("p_total_money", p_total_money);
		map.put("expected_delivery_time", expected_delivery_time);
		map.put("expected_delivery_time_t", expected_delivery_time_t);
		map.put("expected_delivery_time_e", expected_delivery_time_e);
		map.put("p_analyse", p_analyse);
		map.put("p_description", p_description);
		map.put("p_sql", p_sql);
		map.put("p_tm_id", p_tm_id);
		map.put("p_tg_id", p_tg_id);
		map.put("p_cat_ids", p_cat_ids);
		map.put("p_name", p_name);
		map.put("p_file_id", p_file_id);

		_mapper.CreateOrderMlv(map);

		if (map.containsKey("o_error")) {
			int error = (Integer) map.get("o_error");
			if (error == 0) {
				return (String) map.get("o_code");
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
