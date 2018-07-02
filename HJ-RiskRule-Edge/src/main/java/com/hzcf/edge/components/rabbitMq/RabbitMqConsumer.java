//package com.hzcf.edge.components.rabbitMq;
//
//import com.hzcf.api.task.service.TaskService;
//import com.hzcf.ebs.entity.TaskBean;
//import com.hzcf.ebs.util.BeansUtil;
//import com.rabbitmq.client.*;
//
//import java.io.IOException;
//
///**
// * Created by liqinwen on 2017/8/23.
// */
//public class RabbitMqConsumer implements Consumer {
//
//    private TaskService taskService;
//    private Channel channel;
//
//    public RabbitMqConsumer(Channel channel , TaskService taskService)
//    {
//        this.channel = channel;
//        this.taskService = taskService;
//    }
//
//    @Override
//    public void handleConsumeOk(String s) {
//
//    }
//
//    @Override
//    public void handleCancelOk(String s) {
//
//    }
//
//    @Override
//    public void handleCancel(String s) throws IOException {
//
//    }
//
//    @Override
//    public void handleShutdownSignal(String s, ShutdownSignalException e) {
//
//    }
//
//    @Override
//    public void handleRecoverOk(String s) {
//
//    }
//    @Override
//    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
//
//        String routingKey = envelope.getRoutingKey();
//        String contentType = basicProperties.getContentType();
//        long deliveryTag = envelope.getDeliveryTag();
//        TaskBean taskBean = (TaskBean) BeansUtil.toObject(bytes);
//        Action action = Action.RETRY;
//        try{
//            taskService.parserTaskBatch(taskBean);
//            action = Action.ACCEPT;
//            channel.basicAck(deliveryTag, false);
//        }catch (Exception e)
//        {
//           e.printStackTrace();
//        }finally {
////            if(action == Action.RETRY)
////            {
////                channel.basicNack(deliveryTag,false,true);
////            }
//        }
//
//    }
//
//    enum Action {
//        ACCEPT,  // 处理成功
//        RETRY,   // 可以重试的错误
//        REJECT,  // 无需重试的错误
//    }
//}
