package com.wiitrans.base.db.model;

import java.util.HashMap;

public interface ProcTransSettlementMapper {
	// public void Settlement(@Param(value = "p_node_id") int p_node_id,
	// @Param(value = "p_code") String p_code,
	// @Param(value = "o_error") int o_error);
	public void Settlement(HashMap<String, Object> map);
}
