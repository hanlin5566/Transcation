package com.wiitrans.automation.quartz;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import com.wiitrans.base.db.model.AutomationTaskBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;

public class TaskScheduler {
	private BlockingQueue<AutomationTaskBean> queue;
	private SchedulerFactory sf;
	private static TaskScheduler taskScheduler;
	private String groupName = "group1";
	private String jobPrefix = "job_";
	private String triggerPrefix = "trigger_";
	private int delay = 500;
	private boolean isTake = false;
	
	
	public BlockingQueue<AutomationTaskBean> getQueue() {
		return queue;
	}
	private TaskScheduler(){
	}
	
	public static TaskScheduler getInstance() {
		if(taskScheduler == null){
			taskScheduler = new TaskScheduler();
			taskScheduler.init();
			taskScheduler.startScheduler();
		}
		return taskScheduler;
	}
	
	//初始化定时任务容器
	public void init(){
		try {
			//Log4j.info("------- Initializing -------------------");
			//初始化阻塞队列
			queue = new LinkedBlockingDeque<AutomationTaskBean>();
			sf = new StdSchedulerFactory();
			Log4j.info("TaskScheduler Initialization Complete");
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error("TaskScheduler Init Error"+e.getMessage());
		}
	}
	//启动获取队列
	private void takeQueue(){
			// Log4j.info("------- Scheduling Jobs ----------------");
			 Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(isTake){
						int taskId = 0;
						try {
							AutomationTaskBean task = queue.take();
							taskId = task.getTask_id();
							String corn = task.getCorn();
							String param = task.getParam();
							String className = task.getJob_class();
							//new job
							Class<Job> clazz = (Class<Job>) Class.forName(className);
							JobDetail job = JobBuilder.newJob(clazz).withIdentity(jobPrefix+taskId, groupName).build();
							//set param
							JSONObject jsonobj = new JSONObject(param);
							Map<String, Object> map = Util.convert(jsonobj);
							//添加taskID aid method
							map.put("taskId", taskId);
							map.put("aid", "newtask");
							map.put("method", "POST");
							job.getJobDataMap().putAll(map);
							//new trigger
							CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerPrefix+taskId, groupName).withSchedule(CronScheduleBuilder.cronSchedule(corn)).build();
							Scheduler sched = sf.getScheduler();
							Date date = sched.scheduleJob(job, trigger);
							Log4j.info(job.getKey() + " has been scheduled to run at: " + date + " and repeat based on expression: "+ trigger.getCronExpression());
							Thread.sleep(delay);
						} catch (Exception e) {
							// TODO: handle exception
							Log4j.error("excute task error taskId:"+taskId+e.getMessage());
						}
					}
				 }
			 });
			 thread.start();
	}
	
	//重启任务容器
	public boolean rebootScheduler() {
		Log4j.info("------- Reboot Scheduler---------------------");
		boolean ret = false;
		try {
			if(sf != null){
				//停止调度其
				shuttingScheduler();
				//重新初始化调度器
				init();
				//启动调度器
				startScheduler();
				ret = true;
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error("Reboot Scheduler Error"+e.getMessage());
		}
		return ret;
	}
	
	
	//关闭任务容器
	private void shuttingScheduler() {
		Log4j.info("------- Shutting Down Scheduler---------------------");
		try {
			if(sf != null){
				//停止获取队列
				isTake = false;
				Scheduler sched = sf.getScheduler();
				//停止调度器
				sched.shutdown(true);
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error("Shutting Down Scheduler Error"+e.getMessage());
		}
	}
	//启动任务
	private void startScheduler() {
		Log4j.info("------- Starting Scheduler ----------------");
		try {
			if(sf != null){
				//启动获取任务队列
				isTake = true;
				takeQueue();
				Scheduler sched = sf.getScheduler();
				//启动调度器
				sched.start();
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error("Starting Scheduler Error"+e.getMessage());
		}
	}
}
