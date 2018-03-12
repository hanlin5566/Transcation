package com.wiitrans.base.db.model;

public interface RecomOrderBeanMapper {
	public RecomOrderBean Select(String code);

	public String SelectRecomT(int order_id);

	public String SelectRecomE(int order_id);

	public String SelectTeam(int customer_id, int pair_id);

}
