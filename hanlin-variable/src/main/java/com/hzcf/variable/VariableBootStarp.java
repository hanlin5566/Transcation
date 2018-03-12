package com.hzcf.variable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Create by hanlin on 2017年11月6日
 **/
@SpringBootApplication
@EnableSwagger2
public class VariableBootStarp {
	public static void main(String[] args) {
		SpringApplication.run(VariableBootStarp.class, args);
	}
}
