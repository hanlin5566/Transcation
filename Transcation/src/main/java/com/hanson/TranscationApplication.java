package com.hanson;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hanson.**.**.mapper")
public class TranscationApplication {
	public static void main(String[] args) {
		SpringApplication.run(TranscationApplication.class, args);
	}
}

