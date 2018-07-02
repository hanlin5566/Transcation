package com.hzcf.edge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = { "com.hzcf" })
public class RiskRuleEdgeApplication {
	public static void main(String[] args) {
		SpringApplication.run(RiskRuleEdgeApplication.class, args);
	}
	
	/**
	 * 声明一个RestTemplate的bean。
	 * 通过@LoadBalanced注解,表明这个restRemplate开启负载均衡的功能。
	 * @return
	 */
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
