//package com.hzcf.edge.service;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.UUID;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.stereotype.Service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.hzcf.base.result.ResponseData;
//import com.hzcf.cbd.entity.ParamEntity;
//import com.hzcf.ebs.entity.InterfaceRecordEntity;
//import com.hzcf.edge.common.utils.ServletUtil;
//import com.hzcf.edge.common.utils.StringUtils;
//import com.hzcf.edge.entity.UserEntity;
//
///**
// * Create by hanlin on 2018年6月27日
// **/
//@Service
//public class RiskRuleEdgeService {
//	@Autowired
//	public ResponseData decision(JSONObject args){
//		return ResponseData.ok(null);
//	}
//	
//	public void before(MockHttpServletRequest request,final InterfaceRecordEntity recordEntity,UserEntity userEntity,ParamEntity paramEntity,boolean needValidate)
//    {
//        if (paramEntity != null) {
//            recordEntity.setIpAddress("localhost");
//            recordEntity.setQueryParams(paramEntity.getData().toString());
//        }else{
//            recordEntity.setIpAddress(ServletUtil.getIpAddress(request));
//            paramEntity = doBase(request, userEntity, recordEntity,needValidate);
//        }
//        String orderNo ="";
//        if(StringUtils.isNotNull(paramEntity.getLogId()))
//        {
//            orderNo = paramEntity.getLogId();
//        }else{
//            orderNo = UUID.randomUUID().toString();
//        }
//        recordEntity.setQueryTime(new Date());
//        recordEntity.setId(orderNo);
//        recordEntity.setLogId(orderNo);
//        recordEntity.setUserId(userEntity.getUserId());
//        recordEntity.setUserName(userEntity.getUserName());
//        recordEntity.setMobile(paramEntity.getMobile());
//        recordEntity.setIdCard(paramEntity.getIdCard());
//        recordEntity.setName(paramEntity.getName());
//        recordEntity.setCompanyCode(paramEntity.getCompanyCode());
//        recordEntity.setTaskId(paramEntity.getTaskId());
//        recordEntity.setRuleId(paramEntity.getRuleId());
//        recordEntity.setInterfaceType(paramEntity.getInterfaceType());
//        recordEntity.setData(paramEntity.getData()==null? new HashMap(): paramEntity.getData());//保留原始入参
//    }
//}
