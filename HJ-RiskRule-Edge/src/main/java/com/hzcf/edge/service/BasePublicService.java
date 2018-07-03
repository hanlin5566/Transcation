package com.hzcf.edge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.hzcf.ebs.entity.InterfaceRecordEntity;
import com.hzcf.ebs.entity.TaskBean;
import com.hzcf.edge.common.constant.ReturnConst;
import com.hzcf.edge.common.entity.ParamEntity;
import com.hzcf.edge.common.entity.UserEntity;
import com.hzcf.edge.common.exception.ServiceException;
import com.hzcf.edge.common.utils.AESUtil;
import com.hzcf.edge.common.utils.Logs;
import com.hzcf.edge.common.utils.MD5Util;
import com.hzcf.edge.common.utils.ServletUtil;
import com.hzcf.edge.common.utils.StringUtils;
import com.hzcf.edge.components.redis.RedisProvider;

/**
 * Create by hanlin on 2018年6月27日
 **/
@Service
public class BasePublicService {
	@Autowired
	private AuthService authService;

	private static final int hisIndex = 2;

	private static final String ebsQueue = "hjEBSQueue";

	public void before(HttpServletRequest request, final InterfaceRecordEntity recordEntity, UserEntity userEntity,
			ParamEntity paramEntity, boolean needValidate) {
		if (paramEntity != null) {
			recordEntity.setIpAddress("localhost");
			recordEntity.setQueryParams(paramEntity.getData().toString());
		} else {
			recordEntity.setIpAddress(ServletUtil.getIpAddress(request));
			paramEntity = doBase(request, userEntity, recordEntity, needValidate);
		}
		String orderNo = "";
		if (StringUtils.isNotNull(paramEntity.getLogId())) {
			orderNo = paramEntity.getLogId();
		} else {
			orderNo = UUID.randomUUID().toString();
		}
		recordEntity.setQueryTime(new Date());
		recordEntity.setId(orderNo);
		recordEntity.setLogId(orderNo);
		recordEntity.setUserId(userEntity.getUserId());
		recordEntity.setUserName(userEntity.getUserName());
		recordEntity.setMobile(paramEntity.getMobile());
		recordEntity.setIdCard(paramEntity.getIdCard());
		recordEntity.setName(paramEntity.getName());
		recordEntity.setCompanyCode(paramEntity.getCompanyCode());
		recordEntity.setTaskId(paramEntity.getTaskId());
		recordEntity.setRuleId(paramEntity.getRuleId());
		recordEntity.setInterfaceType(paramEntity.getInterfaceType());
		recordEntity.setData(paramEntity.getData() == null ? new HashMap() : paramEntity.getData());// 保留原始入参
	}

	public ParamEntity doBase(final HttpServletRequest request, final UserEntity userEntity,
			final InterfaceRecordEntity interfaceRecordEntity, boolean needValidate) {
		String[] keys = { "idCard", "name", "mobile" };
		return doBase(request, keys, userEntity, interfaceRecordEntity, needValidate);
	}

	public ParamEntity doBase(final HttpServletRequest request, String[] keys, final UserEntity userEntity,
			final InterfaceRecordEntity interfaceRecordEntity, boolean needValidate) {
		Map<String, Object> paramMap = ServletUtil.getParameterMap(request);
		if (paramMap == null)
			throw new ServiceException(ReturnConst.RETCODE_300000, ReturnConst.RETMSG_300000);
		// 请求参数中的data数据体
		JSONObject data = new JSONObject();
		UserEntity userEntityIn = authService.authentication(paramMap);
		interfaceRecordEntity.setUserName(userEntityIn.getUserName());
		interfaceRecordEntity.setUserId(userEntityIn.getUserId());
		UserEntity.copy(userEntity, userEntityIn);
		interfaceRecordEntity.setState("2");

		if (!userEntity.isTest()) {
			data = AESUtil.getDecryptMap(paramMap.get("data").toString(), userEntity.getApiKey());
			if (data == null) {
				throw new ServiceException(ReturnConst.RETCODE_300000, ReturnConst.RETMSG_300000,
						interfaceRecordEntity);
			}
		} else {
			data = JSONObject.parseObject(paramMap.get("data").toString());
		}
		interfaceRecordEntity.setQueryParams(data.toString());
		// 参数校验
		if (needValidate) {
			checkParam(keys, data);
		}
		ParamEntity paramEntity = new ParamEntity();
		if (data.containsKey("logId")) {
			paramEntity.setLogId(data.get("logId").toString());
		}
		paramEntity.setCompanyCode(userEntity.getCompanyCode());
		paramEntity.setTaskId(data.containsKey("taskId") ? data.get("taskId").toString() : "");
		paramEntity.setRuleId(data.containsKey("ruleId") ? data.get("ruleId").toString() : "");
		paramEntity.setIdCard(data.containsKey("idCard") ? data.get("idCard").toString() : "");
		paramEntity.setMobile(data.containsKey("mobile") ? data.get("mobile").toString() : "");
		paramEntity.setName(data.containsKey("name") ? data.get("name").toString() : "");
		paramEntity.setCompany(data.containsKey("company") ? data.get("company").toString() : "");
		paramEntity.setHomeAddr(data.containsKey("homeAddr") ? data.get("homeAddr").toString() : "");
		paramEntity.setEmail(data.containsKey("email") ? data.get("email").toString() : "");
		paramEntity.setInterfaceType(data.containsKey("interfaceType") ? data.get("interfaceType").toString() : "");
		// 其他扩展字段
		paramEntity.setData(data);
		return paramEntity;
	}

	public void checkParam(String[] keys, final Map param) {
		Map error = new HashMap();
		error.put("success", true);
		for (int i = 0; i < keys.length; i++) {
			if (!param.containsKey(keys[i])) {
				throw new ServiceException(ReturnConst.RETCODE_300001, ReturnConst.RETMSG_300001 + ":" + keys[i]);
			}
			switch (keys[i]) {
			case "idCard":
				// idCardValidate(param.get(keys[i]).toString());
				break;
			case "name":
				nameValidate(param.get(keys[i]).toString());
				break;
			case "mobile":
				mobileValidate(param.get(keys[i]).toString());
				break;
			}
		}
	}

	/**
	 * 姓名校验
	 *
	 * @param name
	 * @return
	 */
	private void nameValidate(String name) {
		if (!StringUtils.isNotNull(name)) {
			throw new ServiceException(ReturnConst.RETCODE_300008, ReturnConst.RETMSG_300008);
		}
		// 长度校验
		if (name.length() > 20) {
			throw new ServiceException(ReturnConst.RETCODE_300010, "姓名" + ReturnConst.RETMSG_300010 + " 20个字符");
		}
	}

	/**
	 * 手机号校验
	 *
	 * @param mobile
	 * @return
	 */
	private void mobileValidate(String mobile) {
		if (!StringUtils.isNotNull(mobile)) {
			throw new ServiceException(ReturnConst.RETCODE_300004, ReturnConst.RETMSG_300004);
		}
		// 长度校验
		if (mobile.length() > 20) {
			throw new ServiceException(ReturnConst.RETCODE_300010, "手机号" + ReturnConst.RETMSG_300010 + " 20个字符");
		}
	}

	@Async
	public void addQueryLog(InterfaceRecordEntity interfaceRecordEntity) {
		Date q = interfaceRecordEntity.getQueryTime();
		String h = q.getHours() < 10 ? "0" + q.getHours() : String.valueOf(q.getHours());
		String m = q.getMinutes() < 10 ? "0" + q.getMinutes() : String.valueOf(q.getMinutes());
		String s = q.getSeconds() < 10 ? "0" + q.getSeconds() : String.valueOf(q.getSeconds());
		String HI = h + ":" + m;
		interfaceRecordEntity.setQueryHi(HI);
		interfaceRecordEntity.setQueryHis(HI + ":" + s);
		if (interfaceRecordEntity.getReturnTime() == null)
			interfaceRecordEntity.setReturnTime(new Date());
		interfaceRecordEntity.setTimeUsed(
				interfaceRecordEntity.getReturnTime().getTime() - interfaceRecordEntity.getQueryTime().getTime());
		interfaceRecordEntity.setData(null);
		// 数据总线埋点 todo 进件数据的处理
		TaskBean taskBean = new TaskBean();
		// if (interfaceRecordEntity.getAppOrderEntity() != null) {
		// taskBean.setAppOrderEntity(interfaceRecordEntity.getAppOrderEntity());
		// }
		taskBean.setInterfaceRecordEntity(interfaceRecordEntity);
		taskBean.setTaskId(interfaceRecordEntity.getTaskId());
		//TODO:通过HTTP请求数据总线
//		RabbitMqSender.SendEbs(taskBean, ebsQueue);
	}

	/**
	 * 保存历史数据 缓存时间15天
	 * 
	 * @param recordEntity
	 * @param results
	 */
	public void saveHistory(final InterfaceRecordEntity recordEntity, final String results) {
		StringBuilder k = new StringBuilder();
		k.append(recordEntity.getInterfaceParentType()).append(recordEntity.getInterfaceType())
				.append(recordEntity.getIdCard()).append(recordEntity.getMobile()).append(recordEntity.getName());
		String key = MD5Util.getMD5Result(k.toString());
		RedisProvider.set(key, results, hisIndex, 1296000);
	}

	public void saveHistory(final InterfaceRecordEntity recordEntity) {
		StringBuilder k = new StringBuilder();
		k.append(recordEntity.getInterfaceParentType()).append(recordEntity.getInterfaceType())
				.append(recordEntity.getIdCard()).append(recordEntity.getMobile()).append(recordEntity.getName());
		String key = MD5Util.getMD5Result(k.toString());
		RedisProvider.set(key, recordEntity.getResults(), hisIndex, 1296000);
	}
}
