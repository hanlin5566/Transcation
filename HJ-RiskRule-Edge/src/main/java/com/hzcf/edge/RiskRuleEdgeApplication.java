package com.hzcf.edge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.web.client.RestTemplate;

import com.hzcf.edge.components.redis.RedisClient;

@SpringBootApplication
@ComponentScan(basePackages = { "com.hzcf" })
public class RiskRuleEdgeApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(RiskRuleEdgeApplication.class, args);
        configurableApplicationContext.addApplicationListener(new ApplicationEventListener(configurableApplicationContext));

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

class ApplicationEventListener implements ApplicationListener {
    
	ConfigurableApplicationContext configurableApplicationContext;
	
    public ApplicationEventListener() {
		super();
	}
    public ApplicationEventListener(ConfigurableApplicationContext configurableApplicationContext) {
    	super();
    	this.configurableApplicationContext = configurableApplicationContext;
    }

	@Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 在这里可以监听到Spring Boot的生命周期
        if (event instanceof ContextStoppedEvent) { // 应用停止
        } else if (event instanceof ContextClosedEvent) { // 应用关闭
    		RedisClient redis = configurableApplicationContext.getBean(RedisClient.class);
        	redis.destroy();
        }
    }
}
