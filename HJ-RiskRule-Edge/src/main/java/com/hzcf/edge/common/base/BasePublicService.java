//package com.hzcf.edge.common.base;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Map;
//import java.util.Random;
//import java.util.UUID;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.hzcf.ebs.entity.InterfaceRecordEntity;
//import com.hzcf.ebs.entity.TaskBean;
//import com.hzcf.edge.common.conf.PropertiesConfig;
//import com.hzcf.edge.common.utils.MD5Util;
//import com.hzcf.edge.common.utils.ServletUtil;
//import com.hzcf.edge.components.redis.RedisProvider;
//import com.hzcf.edge.service.mongo.MongoService;
//
//@SuppressWarnings({"unchecked", "rawtypes"})
//@Service("basePublicService")
//public class BasePublicService {
//
//    private static Logger logger = LoggerFactory.getLogger(BasePublicService.class);
//
//    private  final int hisIndex = 2;
//
//    @Value("${baseService.expire}")
//    private  int expire;
//
//    @Value("${baseService.ebsQueue}")
//    private  String ebsQueue;
//
//    @Autowired
//    MongoService mongoService;
//
//    /**
//     * 接口基础处理 返回入参类
//     */
//    public InterfaceRecordEntity before(final HttpServletRequest request) {
//        Map<String, Object> paramMap = ServletUtil.getParameterMap(request);
//        JSONObject data = new JSONObject();
//        InterfaceRecordEntity interfaceRecordEntity = new InterfaceRecordEntity();
//        interfaceRecordEntity.setState("2");
//        data = JSONObject.parseObject(paramMap.get("data").toString());
//        interfaceRecordEntity.setQueryParams(data.toString());
//        if (data.containsKey("logId")) {
//            interfaceRecordEntity.setLogId(data.get("logId").toString());
//        } else {
//            interfaceRecordEntity.setLogId(UUID.randomUUID().toString());
//        }
//        interfaceRecordEntity.setQueryTime(new Date());
//        interfaceRecordEntity.setCompanyCode(data.getString("companyCode"));
//        interfaceRecordEntity.setTaskId(data.containsKey("taskId") ? data.get("taskId").toString() : getOrderIdByTime());
//        interfaceRecordEntity.setRuleId(data.containsKey("ruleId") ? data.get("ruleId").toString() : "");
//        interfaceRecordEntity.setIdCard(data.containsKey("idCard") ? data.get("idCard").toString() : "");
//        interfaceRecordEntity.setMobile(data.containsKey("mobile") ? data.get("mobile").toString() : "");
//        interfaceRecordEntity.setName(data.containsKey("name") ? data.get("name").toString() : "");
//        //其他扩展字段
//        interfaceRecordEntity.setData(data);
//        return interfaceRecordEntity;
//    }
//
//
//    /**
//     * 历史数据 缓存30天
//     *
//     * @param recordEntity
//     * @return
//     */
//    public String getHistory(final InterfaceRecordEntity recordEntity) {
//        StringBuilder k = new StringBuilder();
//        k.append(recordEntity.getInterfaceParentType())
//                .append(recordEntity.getInterfaceType())
//                .append(recordEntity.getIdCard())
//                .append(recordEntity.getMobile())
//                .append(recordEntity.getName());
//        String key = MD5Util.getMD5Result(k.toString());
//        if (RedisProvider.exist(key, hisIndex)) {
//            return RedisProvider.get(key, hisIndex);
//        }
//        return null;
//    }
//
//
//    /**
//     * 保存历史数据 缓存时间30天
//     *
//     * @param recordEntity
//     */
//    public void saveHistory(final InterfaceRecordEntity recordEntity) {
//        StringBuilder k = new StringBuilder();
//        k.append(recordEntity.getInterfaceParentType())
//                .append(recordEntity.getInterfaceType())
//                .append(recordEntity.getIdCard())
//                .append(recordEntity.getMobile())
//                .append(recordEntity.getName());
//        String key = MD5Util.getMD5Result(k.toString());
//        RedisProvider.set(key, recordEntity.getResults(), hisIndex, expire);
//    }
//
//
//    private String getOrderIdByTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        String newDate = sdf.format(new Date());
//        String result = "";
//        Random random = new Random();
//        for (int i = 0; i < 3; i++) {
//            result += random.nextInt(10);
//        }
//        return "TMP" + newDate + result;
//    }
//
//    public String getProperty(String key) {
//        if (PropertiesConfig.propertiesMap.containsKey(key)) {
//            return PropertiesConfig.propertiesMap.get(key);
//        }
//        return null;
//    }
//
//    @Async
//    public void addQueryLog(InterfaceRecordEntity interfaceRecordEntity) {
//        Date q = interfaceRecordEntity.getQueryTime();
//        String h = q.getHours() < 10 ? "0" + q.getHours() : String.valueOf(q.getHours());
//        String m = q.getMinutes() < 10 ? "0" + q.getMinutes() : String.valueOf(q.getMinutes());
//        String s = q.getSeconds() < 10 ? "0" + q.getSeconds() : String.valueOf(q.getSeconds());
//        String HI = h + ":" + m;
//        interfaceRecordEntity.setQueryHi(HI);
//        interfaceRecordEntity.setQueryHis(HI + ":" + s);
//        if (interfaceRecordEntity.getReturnTime() == null) interfaceRecordEntity.setReturnTime(new Date());
//        interfaceRecordEntity.setTimeUsed(interfaceRecordEntity.getReturnTime().getTime()
//                - interfaceRecordEntity.getQueryTime().getTime());
//        interfaceRecordEntity.setData(null);
//        mongoService.saveLog(interfaceRecordEntity, null);
//        if ("decision-start".equals(interfaceRecordEntity.getInterfaceType())
//                || "hzcf-fraudScore".equals(interfaceRecordEntity.getInterfaceType())
//                || "hzcf-creditScore".equals(interfaceRecordEntity.getInterfaceType())) {
//            mongoService.saveLog(interfaceRecordEntity, "log_app_data");
//        }
//        //保存历史数据 统一处理 正常有无数据 都进入缓存
//        if ("2".equals(interfaceRecordEntity.getState()) || "3".equals(interfaceRecordEntity.getState())) {
//            saveHistory(interfaceRecordEntity);
//        }
//        //数据总线埋点 todo 进件数据的处理
//        TaskBean taskBean = new TaskBean();
////        if (interfaceRecordEntity.getAppOrderEntity() != null) {
////            taskBean.setAppOrderEntity(interfaceRecordEntity.getAppOrderEntity());
////        }
////        taskBean.setInterfaceRecordEntity(interfaceRecordEntity);
////        taskBean.setTaskId(interfaceRecordEntity.getTaskId());
////        RabbitMqSender.SendEbs(taskBean, ebsQueue);
//    }
//}
