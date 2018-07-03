package com.hzcf.edge.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.ebs.entity.InterfaceRecordEntity;
import com.hzcf.edge.service.RiskRuleEdgeService;

/**
 * Create by hanlin on 2018年6月27日
 **/
@RestController
@RequestMapping("/public/hzcf/riskRule")
public class RiskRuleEdgeController {

	/**
	 * 注入声明的
	 */
	@Autowired
	private RestTemplate client;

	@Value("${com.hzcf.feathData.serviceId}")
	private String feathDataServiceId;
	
	@Autowired
	private RiskRuleEdgeService riskRuleService;


	@PostMapping
	public InterfaceRecordEntity post(HttpServletRequest request) {
		InterfaceRecordEntity riskRule = riskRuleService.riskRule(request);
		return riskRule;
	}
}
