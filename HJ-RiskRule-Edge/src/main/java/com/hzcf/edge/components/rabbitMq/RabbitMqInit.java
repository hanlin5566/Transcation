//package com.hzcf.edge.components.rabbitMq;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeoutException;
//
//import javax.annotation.PostConstruct;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.hzcf.api.task.service.impl.TaskServiceImpl;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//
///**
// * Created by liqinwen on 2017/8/23.
// */
//@Component
//public class RabbitMqInit {
//
//    private static Logger logger = LoggerFactory.getLogger(RabbitMqInit.class);
//    @Value("${rabbitmq.hosts}")
//    private String hosts;
//    @Value("${rabbitmq.port}")
//    private int port;
//    private int threadsNum = 5;
//    @Value("${rabbitmq.exchangeName}")
//    public static String exchangeName = "hj_exchange";
//    private Connection conn;
//    private static List<Channel> channels =  new ArrayList();
//    @Value("${rabbitmq.virtualHost}")
//    private String virtualHost;
//    @Value("${rabbitmq.userName}")
//    private String userName;
//    @Value("${rabbitmq.password}")
//    private String password;
//    @Value("${rabbitmq.queues}")
//    private String queues;
//    @Value("${rabbitmq.channelNum}")
//    private static int channelNum=20;
//
//    private Boolean autoAsk = false;
//
//    @Autowired
//    private TaskServiceImpl taskService;
//
//    private RabbitMqInit() {
//
//    }
//    @PostConstruct
//    public void init() {
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        connectionFactory.setHost(hosts);
//        connectionFactory.setPort(port);
//        connectionFactory.setUsername(userName);
//        connectionFactory.setPassword(password);
//        //connectionFactory.setRequestedHeartbeat(600);
//        connectionFactory.setRequestedHeartbeat(0);
//        connectionFactory.setVirtualHost(virtualHost);
//        connectionFactory.setConnectionTimeout(100000);
//        ExecutorService es = Executors.newFixedThreadPool(threadsNum);
//        try {
//            conn = connectionFactory.newConnection(es);
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error(" 队列连接失败：" + e.getMessage());
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//            logger.error(" 队列连接超时：" + e.getMessage());
//        }
//        try {
//            for(int c=1;c<=channelNum;c++)
//            {
//                Channel channel = conn.createChannel(c);
//                channel.basicQos(0,1,false);
//                channel.exchangeDeclare(exchangeName, "direct", true);
//                String[] qs = queues.split(",");
//                for (int i = 0; i < qs.length; i++) {
//                    channel.queueDeclare(qs[i], true, false, false, null);
//                    channel.queueBind(qs[i], exchangeName, qs[i]);//一对一
//                    channel.basicConsume(qs[i],autoAsk,new RabbitMqConsumer(channel,taskService));//仅消费端使用
//                }
//                channels.add(channel);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error(" 创建Channel失败 ：" + e.getMessage());
//        }
//        System.out.println("====== RabbitMq 启动成功 ======");
//    }
//
//    public void destroy() {
//        try {
//            channel().close();
//            conn.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public TaskServiceImpl getTaskService() {
//        return taskService;
//    }
//
//    public void setTaskService(TaskServiceImpl taskService) {
//        this.taskService = taskService;
//    }
//
//
//    public static Channel channel() {
//        Random random = new Random();
//        int s = random.nextInt(channelNum-1);
//        return channels.get(s);
//    }
//
//}
