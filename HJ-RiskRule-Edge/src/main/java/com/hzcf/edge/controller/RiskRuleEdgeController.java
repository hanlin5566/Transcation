package com.hzcf.edge.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
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
    RestTemplate client;
	
	@Value("${com.hzcf.feathData.serviceId}")
	String feathDataServiceId;
	
	@GetMapping
    public String get(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		//topo
		String responseEntity = client.postForObject(feathDataServiceId+requestURI, null, String.class);
		System.out.println(responseEntity);
        return responseEntity;
    }
	
	@PostMapping
	public String post(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String responseEntity = client.postForObject(feathDataServiceId+requestURI, null, String.class);
		System.out.println(responseEntity);
		return responseEntity;
	}
}
