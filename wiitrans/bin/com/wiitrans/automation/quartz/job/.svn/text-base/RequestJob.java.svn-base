package com.wiitrans.automation.quartz.job;

import java.util.Date;

import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;

public class RequestJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
	    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
	    try {
	    	String req_type = ""+dataMap.get("req_type");
	    	String url = ""+dataMap.get("url");
	    	String param = ""+dataMap.get("param");
	    	switch (req_type) {
			case "http":
				new HttpSimulator(url).executeMethodTimeOut(param.toString(),3);
				break;
			default:
				break;
			}
	    	JSONObject obj = new JSONObject(dataMap);
	    	Log4j.info("---" + jobKey + " executing at " + new Date() + " with dataMap " + obj.toString());
		} catch (Exception e) {
			Log4j.error("Execute Job Failed"+e.getMessage());
			JobExecutionException e2 = new JobExecutionException(e);
		    e2.setRefireImmediately(true);
		}
	}

}
