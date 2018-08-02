package com.hzcf.edge.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzcf.ebs.entity.InterfaceRecordEntity;
import com.hzcf.edge.common.entity.HttpInvokeResult;
import com.hzcf.edge.common.entity.UserEntity;
import com.hzcf.edge.common.utils.CityResourceUtils;
import com.hzcf.edge.common.utils.ModelMathUtil;
import com.hzcf.edge.common.utils.StringUtils;
import com.hzcf.edge.components.redis.RedisProvider;
import com.hzcf.service.MS_DecistionService;

/**
 * Create by hanlin on 2018年6月27日
 **/
@Service
public class RiskRuleEdgeService {
	@Autowired
	private BasePublicService baseService;

	@Autowired
	private MS_DecistionService ms_decisionService;

	private static String InterfaceParentType = "hzcf";

	public InterfaceRecordEntity riskRule(HttpServletRequest request) {

		final InterfaceRecordEntity recordEntity = new InterfaceRecordEntity();
		final UserEntity userEntity = new UserEntity();
		baseService.before(request, recordEntity, userEntity, null, false);
		/* 接口调用逻辑 */
		// 回传格式的兼容
		InterfaceRecordEntity returnRecordEntity = new InterfaceRecordEntity();
		getParams(recordEntity);
		/* 接口调用逻辑 */
		// 新增系统唯一流水号
		recordEntity.getData().put("logId", recordEntity.getLogId());
		recordEntity.getData().put("companyCode", recordEntity.getCompanyCode());
		// todo 固定执行旧版规则集
		recordEntity.getData().put("applicationType", "DT01");
		JSONObject decisionParam = getDecisionFlow(recordEntity);
		// 調用決策接口
		try {
			JSONObject decisiontree = ms_decisionService.decisiontree(decisionParam);
			// 回传结构改造
			decisiontree.put("logId", recordEntity.getLogId());
			recordEntity.setResults(decisiontree.toJSONString());
			if (decisiontree.getBoolean("success")) {
				JSONObject oldRet = new JSONObject();
				JSONArray hitRules = new JSONArray();
				if (decisiontree.containsKey("totalScore") && decisiontree.containsKey("step")) {
					recordEntity.setState("2");
					oldRet.put("result", 0);
					oldRet.put("hitNum", 0);
					int totalScore = decisiontree.getInteger("totalScore");
					if (totalScore > 0) {
						oldRet.put("result", totalScore / 100);
						oldRet.put("hitNum", totalScore / 100);
						JSONArray steps = decisiontree.getJSONArray("step");
						String decisionDesc = "";
						for (Object step : steps) {
							JSONObject s = JSONObject.parseObject(step.toString());
							JSONObject rule = JSONObject.parseObject(s.getString("rule"));
							JSONArray hitRules_ = JSONArray.parseArray(rule.getString("hitRules"));
							for (Object hitRule_ : hitRules_) {
								JSONObject r = JSONObject.parseObject(hitRule_.toString());
								JSONObject hitRule = new JSONObject();
								hitRule.put("ruleID", r.get("ruleId"));
								hitRule.put("ruleDesc", r.get("ruleDescribe"));
								hitRule.put("ruleMessage", r.get("ruleDescribe"));
								hitRule.put("hitCode", r.get("ruleId"));
								hitRules.add(hitRule);
							}
							decisionDesc += "命中规则集:" + s.getString("ruleGroup");
						}
					}
					oldRet.put("hitRules", hitRules);
					returnRecordEntity.setResults(oldRet.toJSONString());
				} else {
					recordEntity.setState("1");
					recordEntity.setErrorReturn("反欺诈检查未知异常");
				}

			} else {
				recordEntity.setState("1");
				recordEntity.setErrorReturn(decisiontree.getString("message"));
				if(decisiontree.getString("message") == null) {
					recordEntity.setErrorReturn("反欺诈检查异常");
				}
			}

		} catch (Exception e) {
			//TODO:网络异常返回值处理
			recordEntity.setState("1");
//			recordEntity.setErrorReturn(result.getUrl() + result.getStatusCode() + result.getReason());
			recordEntity.setState("1");
//			recordEntity.setErrorReturn("决策引擎回传json格式有误：" + result.getContentString());
		}
		recordEntity.setInterfaceParentType(InterfaceParentType);
		recordEntity.setInterfaceType("decision-start");
		Date createTime = new Date();
		recordEntity.setReturnTime(createTime);
		// mysql 存储进件信息包括决策结果 重复进件问题
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//		baseService.addQueryLog(recordEntity);
		returnRecordEntity.setState(recordEntity.getState());
		returnRecordEntity.setErrorReturn(recordEntity.getErrorReturn());
		return returnRecordEntity;
	}

	/**
	 * 获取决策流
	 * 
	 * @return
	 */
	private JSONObject getDecisionFlow(InterfaceRecordEntity recordEntity) {
		String decisionKey = recordEntity.getData().getOrDefault("applicationType", "DT01") + "-"
				+ recordEntity.getCompanyCode();
		JSONObject decisionParam = new JSONObject();
		decisionParam.put("param", JSONObject.toJSON(recordEntity.getData()).toString());
		String topo = RedisProvider.get(decisionKey, 0);
		decisionParam.put("topo", topo);
		return decisionParam;
	}

	/**
	 * 信审 线下进件数据转换
	 * 
	 * @param recordEntity
	 */
	private void getParams(final InterfaceRecordEntity recordEntity) {
		JSONObject cli_work_order_apply = JSONObject
				.parseObject(recordEntity.getData().get("cli_work_order_apply").toString());

		JSONArray cli_contact = null;
		if (recordEntity.getData().containsKey("cli_contact")) {
			cli_contact = JSONArray.parseArray(recordEntity.getData().get("cli_contact").toString());
		}
		JSONObject cli_borrower = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String applySubmitTime = df.format(new Date());
		try {
			JSONArray cli_borrowers = JSONArray.parseArray(recordEntity.getData().get("cli_borrower").toString());
			for (Object borrower : cli_borrowers) {
				JSONObject borrower_ = JSONObject.parseObject(borrower.toString());
				if ("0".equals(borrower_.getString("borrower_type"))) {
					cli_borrower = borrower_;
					cli_borrower.put("is_togetherborrow", "1");
				}
			}
			// 有共借人信息
			if (cli_borrowers.size() > 1)
				cli_borrower.put("is_togetherborrow", "0");
			if (!cli_work_order_apply.containsKey("apply_submit_time"))
				cli_work_order_apply.put("apply_submit_time", applySubmitTime);

		} catch (Exception e) {
			// TODO 兼容测试
			cli_borrower = JSONObject.parseObject(recordEntity.getData().get("cli_borrower").toString());
			cli_borrower.put("is_togetherborrow", "1");

			if (!cli_work_order_apply.containsKey("apply_submit_time"))
				cli_work_order_apply.put("apply_submit_time", applySubmitTime);
		}

		Map<String, String> cityIdNameMap = CityResourceUtils.getCityIdNameMap();
		String city_id_name = cityIdNameMap.getOrDefault(cli_work_order_apply.getString("city_id"), "");

		// 相似度变量
		Map<String, String> provinceCityDistrictIdNameMap = CityResourceUtils.getProvinceCityDistrictIdNameMap();
		// 规则20 户籍所在地与进件城市相似性计算
		String domicile_province = cli_borrower.getOrDefault("domicile_province", "").toString();
		String domicile_city = cli_borrower.getOrDefault("domicile_city", "").toString();
		String domicile_district = cli_borrower.getOrDefault("domicile_district", "").toString();
		String domicile_province_name = provinceCityDistrictIdNameMap.getOrDefault(domicile_province, "");
		String domicile_city_name = provinceCityDistrictIdNameMap.getOrDefault(domicile_city, "");
		String domicile_district_name = provinceCityDistrictIdNameMap.getOrDefault(domicile_district, "");
		String domicile_address = domicile_province_name + domicile_city_name + domicile_district_name;
		// creditScoreMod01.setSim_domicile_work(ModelMathUtil.getSimilitude(domicile_address,
		// city_id_name));//TODO
		cli_borrower.put("sim_domicile_work",
				Double.toString(ModelMathUtil.getSimilitude(domicile_address, city_id_name)));
		// 居住地与进件城市相似性计算
		String resident_province = cli_borrower.getOrDefault("resident_province", "").toString();
		String resident_city = cli_borrower.getOrDefault("resident_city", "").toString();
		String resident_district = cli_borrower.getOrDefault("resident_district", "").toString();
		String resident_province_name = provinceCityDistrictIdNameMap.getOrDefault(resident_province, "");
		String resident_city_name = provinceCityDistrictIdNameMap.getOrDefault(resident_city, "");
		String resident_district_name = provinceCityDistrictIdNameMap.getOrDefault(resident_district, "");
		String resident_address = resident_province_name + resident_city_name + resident_district_name;
		// creditScoreMod01.setSim_resident_work(ModelMathUtil.getSimilitude(resident_address,
		// city_id_name));
		cli_borrower.put("sim_resident_work",
				Double.toString(ModelMathUtil.getSimilitude(resident_address, city_id_name)));
		// 工作地与进件城市相似性计算
		String org_province = cli_borrower.getOrDefault("org_province", "").toString();
		String org_city = cli_borrower.getOrDefault("org_city", "").toString();
		String org_district = cli_borrower.getOrDefault("org_district", "").toString();
		String org_province_name = provinceCityDistrictIdNameMap.getOrDefault(org_province, "");
		String org_city_name = provinceCityDistrictIdNameMap.getOrDefault(org_city, "");
		String org_district_name = provinceCityDistrictIdNameMap.getOrDefault(org_district, "");
		String org_address = org_province_name + org_city_name + org_district_name;
		cli_borrower.put("sim_org_work", Double.toString(ModelMathUtil.getSimilitude(org_address, city_id_name)));

		JSONObject params = new JSONObject();
		params.put("cli_work_order_apply", cli_work_order_apply);
		params.put("cli_borrower", cli_borrower);
		if (cli_contact != null)
			params.put("cli_contact", cli_contact);
		recordEntity.setQueryParams(params.toJSONString());
		recordEntity.setIdCard(cli_borrower.getString("id_num"));
		recordEntity.setName(cli_borrower.getString("name"));
		recordEntity.setMobile(cli_borrower.getString("mobile1"));
		recordEntity.setTaskId(cli_borrower.getString("apply_id"));
		recordEntity.setData(new HashMap());
		recordEntity.getData().put("taskId", recordEntity.getTaskId());
		recordEntity.getData().put("idCard", recordEntity.getIdCard());
		recordEntity.getData().put("name", recordEntity.getName());
		recordEntity.getData().put("mobile", recordEntity.getMobile());
		recordEntity.getData().put("applicationSubmitTime", cli_work_order_apply.getString("apply_submit_time"));

		recordEntity.getData().put("birthday",
				cli_borrower.containsKey("birthday") ? cli_borrower.getString("birthday") : "");
		recordEntity.getData().put("gender",
				cli_borrower.containsKey("gender") ? cli_borrower.getString("gender") : "");
		recordEntity.getData().put("idCardValidDate", "");
		recordEntity.getData().put("education",
				cli_borrower.containsKey("education") ? cli_borrower.getString("education") : "");
		recordEntity.getData().put("domicile",
				cli_borrower.containsKey("domicile_address") ? cli_borrower.getString("domicile_address") : "");
		recordEntity.getData().put("maritalStatus",
				cli_borrower.containsKey("marriage_status") ? cli_borrower.getString("marriage_status") : "");

		// 联系人信息处理
		if (cli_contact != null && cli_contact.size() > 0) {
			for (Object o : cli_contact) {
				JSONObject contact = JSONObject.parseObject(o.toString());
				String borrowerTypeSel = contact.getString("borrower_type_sel");
				String contactName = !"null".equals(contact.getString("name"))
						&& StringUtils.isNotNull(contact.getString("name")) ? contact.getString("name") : null;
				String contactTel = !"null".equals(contact.getString("tel_code"))
						&& StringUtils.isNotNull(contact.getString("tel_code")) ? contact.getString("tel_code") : null;

				switch (borrowerTypeSel) {
				case "4":
					if (contactName != null && contactTel != null) {
						recordEntity.getData().put("workingContactName", contactName);
						recordEntity.getData().put("workingContactMobile", contactTel);
					}
					;
					break;
				case "3":
					if (contactName != null && contactTel != null) {
						recordEntity.getData().put("familyContactName", contactName);
						recordEntity.getData().put("familyContactMobile", contactTel);
					}
					;
					break;
				case "5":
					if (contactName != null && contactTel != null) {
						recordEntity.getData().put("emergencyContactName", contactName);
						recordEntity.getData().put("emergencyContactMobile", contactTel);
					}
					;
					break;
				}
			}
		}
		recordEntity.getData().put("company",
				cli_borrower.containsKey("org_name") ? cli_borrower.getString("org_name") : "");
		recordEntity.getData().put("companyAddr",
				cli_borrower.containsKey("org_address") ? cli_borrower.getString("org_address") : "");
		recordEntity.getData().put("companyTel",
				cli_borrower.containsKey("org_tel_code")
						? cli_borrower.getString("org_tel_area_code") + "-" + cli_borrower.getString("org_tel_code")
						: "");
		recordEntity.getData().put("homeTel",
				cli_borrower.containsKey("resident_tel_code")
						? cli_borrower.getString("resident_tel_area_code") + "-"
								+ cli_borrower.getString("resident_tel_code")
						: "");
		recordEntity.getData().put("homeAddr",
				cli_borrower.containsKey("resident_address") ? cli_borrower.getString("resident_address") : "");
		recordEntity.getData().put("loanUsage",
				cli_work_order_apply.containsKey("loan_purpose_desc")
						? cli_work_order_apply.getString("loan_purpose_desc")
						: "");
		recordEntity.getData().put("loanAmount",
				cli_work_order_apply.containsKey("max_amount")
						? cli_work_order_apply.getString("min_amount") + "-"
								+ cli_work_order_apply.getString("max_amount")
						: "");

	}
}
