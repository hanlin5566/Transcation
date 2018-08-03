package com.hzcf.edge.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.edge.common.constant.ReturnConst;
import com.hzcf.edge.common.entity.UserEntity;
import com.hzcf.edge.common.exception.ServiceException;
import com.hzcf.edge.common.utils.MD5Util;
import com.hzcf.edge.common.utils.StringUtils;
import com.hzcf.edge.components.redis.RedisProvider;
import com.hzcf.edge.dao.AuthDAO;

/**
 * Create by hanlin on 2018年7月2日
 **/
@Service
public class AuthService {
	@Autowired
	private AuthDAO authDAO;
	
	
	
	public UserEntity authentication(final Map<String,Object> paramMap) {
      String account = StringUtils.getStringValue(paramMap.get("account"));
      String signatuer = StringUtils.getStringValue(paramMap.get("signature"));
      if (!StringUtils.isNotNull(account)) {
          throw new ServiceException(ReturnConst.RETCODE_100001,ReturnConst.RETMSG_100001);
      }
      if (!StringUtils.isNotNull(signatuer)) {
          throw new ServiceException(ReturnConst.RETCODE_100002,ReturnConst.RETMSG_100002);
      }
      
      //使用redis缓存用户数据
      String userEntityJson = RedisProvider.get(account);
      UserEntity userEntity;
      if(StringUtils.isNotNull(userEntityJson)) {
    	  userEntity = JSONObject.toJavaObject(JSONObject.parseObject(userEntityJson), UserEntity.class);
      }else {
    	  userEntity = authDAO.findByUserIdAndIsValid(account, true);
    	  RedisProvider.set(account, JSONObject.toJSONString(userEntity), 30*60*1000);
      }
      
      if (userEntity==null) {
          throw new ServiceException(ReturnConst.RETCODE_100003,ReturnConst.RETMSG_100003+account+"-"+signatuer);
      }
      String passWord = userEntity.getUserPwd();
      String salt = userEntity.getUserSalt();
      String passWordParam = MD5Util.encodePwd(signatuer, salt);
      if (!passWordParam.equals(passWord)) {
          throw new ServiceException(ReturnConst.RETCODE_100000,ReturnConst.RETMSG_100000);
      }
      return userEntity;
  }
  
}
