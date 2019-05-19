package com.hanson.controller;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanson.TranscationApplication;

@RunWith(SpringJUnit4ClassRunner.class)  
@SpringBootTest(classes = TranscationApplication.class)
@WebAppConfiguration  
public class UserAccountRecordControllerTest {
	MockMvc mock;  
	  
    @Autowired  
    WebApplicationContext webApplicationConnect;  
  
    String expectedJson;  
  
    @Before  
    public void setUp() throws JsonProcessingException {  
    	mock = MockMvcBuilders.webAppContextSetup(webApplicationConnect).build();  
  
    }  
    
	@Test
	public void testRecharge() throws Exception {
		JSONObject param = JSONObject.parseObject("{\r\n" + 
				"        \"userId\": \"86\",\r\n" + 
				"        \"money\": \"1\"\r\n" + 
				"}");
		JSONObject param1 = JSONObject.parseObject("{\r\n" + 
				"        \"userId\": \"80\",\r\n" + 
				"        \"money\": \"1\"\r\n" + 
				"}");
		long s = System.currentTimeMillis();
		//开启线程，模拟并发充值
		int threadCount = 10;//10个线程
		final CountDownLatch latch = new CountDownLatch(threadCount*2);
		for (int i = 1; i <= threadCount; i++) {
			new Thread() {
				@Override
				public void run() {
					try {
						mock
						//发送请求
						.perform(MockMvcRequestBuilders.post("/api/account")
						//指定头和内容并请求
						.contentType(MediaType.APPLICATION_JSON_UTF8).content(param.toJSONString()).accept(MediaType.APPLICATION_JSON_UTF8)).
//						//处理结果
//						andDo(MockMvcResultHandlers.print()).
						//断言
						andExpect(MockMvcResultMatchers.status().isOk())
//						.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
//						.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
						.andReturn();
						latch.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
		for (int i = 1; i <= threadCount; i++) {
			new Thread() {
				@Override
				public void run() {
					try {
						mock
						//发送请求
						.perform(MockMvcRequestBuilders.post("/api/account")
						//指定头和内容并请求
						.contentType(MediaType.APPLICATION_JSON_UTF8).content(param1.toJSONString()).accept(MediaType.APPLICATION_JSON_UTF8)).
//						//处理结果
//						andDo(MockMvcResultHandlers.print()).
						//断言
						andExpect(MockMvcResultMatchers.status().isOk())
//						.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
//						.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
						.andReturn();
						latch.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
		}
		
		latch.await();
		long e = System.currentTimeMillis() - s;
		System.err.println("耗时:"+e);
	}

}
