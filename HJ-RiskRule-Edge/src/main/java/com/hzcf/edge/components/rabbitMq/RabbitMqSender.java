//package com.hzcf.edge.components.rabbitMq;
//
//import java.io.IOException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.hzcf.ebs.entity.TaskBean;
//import com.hzcf.ebs.util.BeansUtil;
//
///**
// * Created by liqinwen on 2017/8/23.
// */
//public class RabbitMqSender {
//
//
//    private static Logger logger = LoggerFactory.getLogger(RabbitMqSender.class);
//
//    public static void Send(TaskBean taskBean, String queue)
//    {
//        try {
//            RabbitMqInit.channel().basicPublish(RabbitMqInit.exchangeName,queue,null, BeansUtil.toByteArray(taskBean));
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("RabbitMq Send 发送失败:"+e.getMessage()+taskBean);
//        }
//    }
//
//    //
//    public static void SendEbs(com.hzcf.ebs.entity.TaskBean taskBean, String queue)
//    {
//        try {
//            RabbitMqInit.channel().basicPublish(RabbitMqInit.exchangeName,queue,null, BeansUtil.toByteArray(taskBean));
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("RabbitMq SendEbs 发送失败:"+e.getMessage()+taskBean);
//        }
//    }
//
//}
