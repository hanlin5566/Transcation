package com.wiitrans.exam.bolt;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.db.ExamDAO;
import com.wiitrans.base.db.model.ExamPaperBean;
import com.wiitrans.base.db.model.ExamQuesBean;
import com.wiitrans.base.db.model.JudgeBean;
import com.wiitrans.base.db.model.StrategyBean;
import com.wiitrans.base.db.model.SubjectiveBean;
import com.wiitrans.base.db.model.VoteBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

@SuppressWarnings("serial")
public class ExamBolt extends BaseBasicBolt {

    private TaskReportor _reportor = null;
    private String _msgURL = "";
    private int _timeout = 2;

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
	WiitransConfig.getInstance(0);
	if (_reportor == null) {
	    _reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
		    BundleConf.EXAM_BUNDLE_PORT);
	    _reportor.Start();
	}
	ConfigNode myNode = BundleConf.BUNDLE_Node.get(BundleConf.DEFAULT_NID);
	if (myNode != null) {
	    _msgURL = myNode.api + "msg/sysMsgCount/";
	}
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
	String content = tuple.getStringByField("content");
	JSONObject obj = new JSONObject(content);
	String aid = Util.GetStringFromJSon("aid", obj);
	String method = Util.GetStringFromJSon("method", obj);

	Log4j.log("ExamBolt " + obj.toString());
	switch (method) {
	case "POST": {
	    switch (aid) {
	    case "start": {
		startExam(obj);
		break;
	    }
	    case "evalUnKnowJudge": {
		evalUnKnowJudge(obj);
		break;
	    }
	    case "evalSubjective": {
		evalSubjective(obj);
		break;
	    }
	    case "commitExam": {
		commitExam(obj);
		break;
	    }
	    case "deleteExam": {
		deleteExam(obj);
		break;
	    }
	    default:
		break;
	    }
	    break;
	}
	default:
	    break;
	}
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declare(new Fields("content"));
    }

    private void sendExamTime(JSONObject obj, int transId, String msg) {
	try {
	    JSONObject param = new JSONObject();
	    param.put("nid", ""+BundleConf.DEFAULT_NID);
	    param.put("tnid", ""+BundleConf.DEFAULT_NID);
	    param.put("uid", obj.get("uid")==null?"0001":""+obj.get("uid"));
	    param.put("sid", obj.get("sid")==null?"testsid":""+obj.get("sid"));
	    param.put("datetype", ""+0);
	    param.put("senduid", ""+transId);
	    param.put("message", msg);
	    new HttpSimulator(_msgURL).executeMethodTimeOut(param.toString(),
		    _timeout);
	} catch (Exception e) {
	    Log4j.error("sendExamTime FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}

    }

    /**
     * #4159 每天定时删除未完成试卷，并且通知议员剩余考试次数
     * 
     * @param obj
     */
    private void deleteExam(JSONObject obj) {
	try {
	    ExamDAO examDAO = new ExamDAO();
	    examDAO.Init(true);
	    List<ExamPaperBean> examPaperBeans = examDAO.selectAllUnFinishedExamPaper();
	    for (ExamPaperBean examPaperBean : examPaperBeans) {
		deleteExamPaper(obj,examDAO, examPaperBean);
	    }
	    examDAO.UnInit();
	    Log4j.info("deleteExam SUCCESS");
	    SendToPHP(obj, "OK");
	} catch (Exception e) {
	    Log4j.error("deleteExam FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
    }

    /**
     * #4158，4157 只有提交后才作为有效答案，并且判断是否达到有效比率来判出主观题
     * 
     * @param obj
     */
    private void commitExam(JSONObject obj) {
	try {
	    int test_id = Util.GetIntFromJSon("test_id", obj);
	    ExamDAO examDAO = new ExamDAO();
	    examDAO.Init(true);
	    // 查找未知判断题
	    List<ExamQuesBean> examQuesBeans = examDAO
		    .selectStrategyByExamId(test_id);
	    for (ExamQuesBean examQuesBean : examQuesBeans) {
		JSONObject json = new JSONObject();
		json.put("test_ques_id", examQuesBean.getTest_ques_id());
		json.put("strategy_id", examQuesBean.getQuestion_id());
		json.put("ujudge", examQuesBean.getJudge());
		this.evalUnKnowJudge(json);
	    }
	    Log4j.info("commitExam SUCCESS");
	    SendToPHP(obj, "OK");
	    examDAO.UnInit();
	} catch (Exception e) {
	    Log4j.error("commitExam FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
    }

    /**
     * 后台判主观题关联的判断题 1.判相关联的选择题分数 2.判此主观题分数 3.判卷子分数 4.删除投票数据 5.是否移入已知判断题
     * 6.删除未知判断题
     * 
     * @param obj
     */
    private void evalSubjective(JSONObject obj) {
	try {
	    // int nid = Util.GetIntFromJSon("nid", obj);
	    // String sid = Util.GetStringFromJSon("sid", obj);
	    // int uid = Util.GetIntFromJSon("uid", obj);
	    int strategy_id = Util.GetIntFromJSon("strategy_id", obj);
	    int move = Util.GetIntFromJSon("move", obj);
	    int ujudge = Util.GetIntFromJSon("ujudge", obj);// 用户的答案 1对 2错
	    ExamDAO examDAO = new ExamDAO();
	    examDAO.Init(true);
	    Map<String, Integer> param = new HashMap<String, Integer>();
	    param.put("strategy_id", strategy_id);
	    param.put("judge", ujudge);
	    // 自动判选择题
	    examDAO.evalJudge(param);
	    // 自动判主管题
	    examDAO.evalSubjective(param);
	    // 更新试卷分数
	    examDAO.updateAutoExamPaperScore(strategy_id);
	    // 判断是否判完所有卷子题，并更新状态（11.申请测试 12.审核 13.通过 14.未通过）
	    examDAO.updateAutoExamPaperStatus(strategy_id);
	    // 删除投票数据
	    examDAO.deleteVote(strategy_id);
	    if (1 == move) {
		// 移入已知选择题库
		examDAO.movetoKnownJudge(param);
	    }
	    // 删除未知判断题
	    examDAO.deleteUnknowJudge(strategy_id);
	    examDAO.Commit();
	    examDAO.UnInit();
	    Log4j.info("evalSubjective SUCCESS");
	    SendToPHP(obj, "OK");
	} catch (Exception e) {
	    Log4j.error("evalSubjective FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
    }

    /**
     * 判未知判断题 1.将答案写入卷子试题(由PHP写入，判断已知判断题通过率合格才会请求此方法，过滤劣质答案） 2.看投票是否超过阈值
     * 3.等于阈值将自动判断答案写入未知判断题题库 4.修改相应试题关系人的分数 5.小于阈值将投票写入投票表 6.大于阈值不做处理
     * 
     * mark: 自动移 如果入已知判断题库需要修改卷子的ID以及类型 mark:
     * 不移动的话会改变结果（如：目前阈值是5MAX是7比分3：2又有人写入两个答案 3:4 需提前判断此题是否已经有答案 不记录投票
     * 
     * @param obj
     */
    private void evalUnKnowJudge(JSONObject obj) {
	try {
	    // int nid = Util.GetIntFromJSon("nid", obj);
	    // String sid = Util.GetStringFromJSon("sid", obj);
	    // int uid = Util.GetIntFromJSon("uid", obj);
	    int test_ques_id = Util.GetIntFromJSon("test_ques_id", obj);
	    int strategy_id = Util.GetIntFromJSon("strategy_id", obj);
	    int ujudge = Util.GetIntFromJSon("ujudge", obj);// 用户的答案 1对 2错
	    ExamDAO examDAO = new ExamDAO();
	    examDAO.Init(true);
	    // 判断此题是否已经被后台人工判过
	    ExamQuesBean examQuesBean = examDAO
		    .selectSubjectByStrategyId(strategy_id);
	    int personalStrategyAns = -1;
	    if (examQuesBean != null && examQuesBean.getScore() != -1) {
		personalStrategyAns = examQuesBean.getScore() > 0 ? 1 : 2;// 如果主观题得分则认为对1否则认为错2
	    }
	    if (personalStrategyAns > 0) {
		// 后台已经判题，以后台答案为准
		Map<String, Integer> param = new HashMap<String, Integer>();
		param.put("strategy_id", strategy_id);
		param.put("judge", personalStrategyAns);
		param.put("test_ques_id", test_ques_id);
		examDAO.evalJudge(param);
		// 更新试卷分数
		examDAO.updateAutoExamPaperScore(strategy_id);
		// 判断是否判完所有卷子题，并更新状态（11.申请测试 12.审核 13.通过 14.未通过）
		examDAO.updateAutoExamPaperStatus(strategy_id);
	    } else {
		// 系统判卷
		Map<String, BigDecimal> voteCount = examDAO
			.getJudgeVoteCount(strategy_id);
		int rightCount = voteCount != null ? voteCount.get("right")
			.intValue() : 0;
		int wrongCount = voteCount != null ? voteCount.get("wrong")
			.intValue() : 0;
		int count = rightCount + wrongCount;
		int unknownJudgeThreshold = WiitransConfig.getInstance(0).EXAM.EXAM_THRESHOLD;
		if (count < (unknownJudgeThreshold - 1)) {
		    // 小于阈值将投票写入投票表
		    VoteBean voteBean = new VoteBean();
		    voteBean.setTest_ques_id(test_ques_id);
		    voteBean.setStrategy_id(strategy_id);
		    voteBean.setType(2);
		    voteBean.setJudge(ujudge);
		    examDAO.addVote(voteBean);
		} else if (count == (unknownJudgeThreshold - 1)) {// 算上自己的答案所以减一
		    // 将自己的投票写入投票表
		    VoteBean voteBean = new VoteBean();
		    voteBean.setTest_ques_id(test_ques_id);
		    voteBean.setStrategy_id(strategy_id);
		    voteBean.setType(2);
		    voteBean.setJudge(ujudge);
		    examDAO.addVote(voteBean);
		    // 加上自己票数
		    if (ujudge == Const.EXAM_JUDGE_RIGHT) {
			rightCount++;
		    } else {
			wrongCount++;
		    }
		    // 触发自动判题-将别人的答题添加分数
		    int judge = rightCount > wrongCount ? Const.EXAM_JUDGE_RIGHT
			    : Const.EXAM_JUDGE_WRONG;
		    Map<String, Integer> param = new HashMap<String, Integer>();
		    param.put("strategy_id", strategy_id);
		    param.put("judge", judge);
		    // 自动判选择题
		    examDAO.evalJudge(param);
		    // 自动判主管题
		    examDAO.evalSubjective(param);
		    // 更新试卷分数
		    examDAO.updateAutoExamPaperScore(strategy_id);
		    // 判断是否判完所有卷子题，并更新状态（1.申请测试 2.审核 3.通过 4.未通过）
		    examDAO.updateAutoExamPaperStatus(strategy_id);
		    // 添加自动判题时间与状态
		    examDAO.updateAutoJudge(strategy_id);
		} else {
		    // 超过阈值避免更改答案，只判断自己的分数，并且不写入投票表
		    int judge = rightCount > wrongCount ? Const.EXAM_JUDGE_RIGHT
			    : Const.EXAM_JUDGE_WRONG;
		    Map<String, Integer> param = new HashMap<String, Integer>();
		    param.put("strategy_id", strategy_id);
		    param.put("judge", judge);
		    param.put("test_ques_id", test_ques_id);
		    examDAO.evalJudge(param);
		}
	    }
	    examDAO.Commit();
	    examDAO.UnInit();
	    Log4j.info("evalUnKnowJudge SUCCESS");
	    SendToPHP(obj, "OK");
	} catch (Exception e) {
	    Log4j.error("evalUnKnowJudge FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
    }

    /**
     * 出题： 1.随机从已知判断题拿出EXAM_COUNT*50%道题
     * 2.随机从未知判断题拿出EXAM_COUNT*30%道题,如不足从已知判断题取剩余题。 3.随机从主观题拿出EXAM_COUNT*20%道题
     * 
     * @param obj
     */
    private void startExam(JSONObject obj) {
	try {
	    // int nid = Util.GetIntFromJSon("nid", obj);
	    // String sid = Util.GetStringFromJSon("sid", obj);
	    int uid = Util.GetIntFromJSon("uid", obj);
	    int pair_id = Util.GetIntFromJSon("pair_id", obj);
	    int industry_id = Util.GetIntFromJSon("industry_id", obj);
	    long s = System.currentTimeMillis();
	    ExamDAO examDAO = new ExamDAO();
	    examDAO.Init(true);
	    // 查找未完成的试题
	    ExamPaperBean paper = new ExamPaperBean();
	    paper.setTranslator_id(uid);
	    paper.setPair_id(pair_id);
	    paper.setIndustry_id(industry_id);
	    // #4155 每月只允许考N次 N=3
	    int monthTimes = examDAO.selectExamMonthTimes(paper);
	    if (monthTimes >= WiitransConfig.getInstance(0).EXAM.EXAM_MONTH_TIEMS) {
		Log4j.debug("out month times");
		JSONObject resObj = new JSONObject();
		resObj.put("result", "FAILED");
		resObj.put("monthTimes", monthTimes);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
			Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID, Util.GetStringFromJSon(
			Const.BUNDLE_INFO_ACTION_ID, obj));
		_reportor.Report(resObj);
		return;
	    }
	    ExamPaperBean examPaper = examDAO.selectUnFinishedExamPaper(paper);
	    Log4j.debug("selectUnFinishedExamPaper -->"
		    + (System.currentTimeMillis() - s) + "ms");
	    s = System.currentTimeMillis();
	    if (examPaper != null) {
		long examUesdTime = (System.currentTimeMillis() / 1000)
			- examPaper.getCreate_time();// 考试用时
		long examTimeOut = WiitransConfig.getInstance(0).EXAM.EXAM_TIMEOUT * 60;// 考试时间限制
		// 过期卷子
		if (examUesdTime > examTimeOut) {
		    deleteExamPaper(obj,examDAO, examPaper);
		    // 创建新卷子
		    createExamPaper(examDAO, paper);
		} else {
		    // 正在答题卷子
		    paper = examPaper;
		}
		Log4j.debug("existExamPaper -->"
			+ (System.currentTimeMillis() - s) + "ms");
		s = System.currentTimeMillis();
	    } else {
		// 新卷子 添加卷子
		createExamPaper(examDAO, paper);
		Log4j.debug("createExamPaper -->"
			+ (System.currentTimeMillis() - s) + "ms");
		s = System.currentTimeMillis();
	    }
	    examDAO.UnInit();
	    Log4j.info("createExamPaper success");
	    JSONObject resObj = new JSONObject();
	    resObj.put("result", "OK");
	    resObj.put("test_id", String.valueOf(paper.getTest_id()));
	    resObj.put("step", String.valueOf(paper.getStep()));
	    resObj.put("monthTimes", monthTimes);
	    resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
	    resObj.put(Const.BUNDLE_INFO_ID,
		    Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
	    resObj.put(Const.BUNDLE_INFO_ACTION_ID,
		    Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
	    _reportor.Report(resObj);
	    Log4j.info("send report:" + resObj);
	} catch (Exception e) {
	    Log4j.error("startExam FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
	//
    }

    private void deleteExamPaper(JSONObject obj,ExamDAO examDAO, ExamPaperBean examPaperBean){
	try {
	    // 删除未知判断题与投票
	    examDAO.deleteUnFinishedJudgeVote(examPaperBean.getTest_id());
	    // 删除主观题答案
	    examDAO.deleteUnFinishedSubjectiveAnswer(examPaperBean.getTest_id());
	    // 删除试题
	    examDAO.deleteUnFinishedExamQues(examPaperBean.getTest_id());
	    // 删除卷子
	    examDAO.deleteUnFinishedExamPaper(examPaperBean.getTest_id());
	    
	    // 按源对-领域通知议员考试次数
	    Map<String, String> monthTimes = examDAO.selectExamMonthTimesMsg(examPaperBean);
	    String msg = "";
	    int examCount = WiitransConfig.getInstance(0).EXAM.EXAM_COUNT;
	    int times = Integer.valueOf(String.valueOf(monthTimes.get("times")));
	    int myOverplusTimes = examCount - times;//剩余考试次数
	    String industry_name = monthTimes.get("industry_name");
	    String source_lange = monthTimes.get("source_lange");
	    String target_lang = monthTimes.get("target_lang");
	    String pair_name = source_lange+">"+target_lang;
	    if(myOverplusTimes>0){
		msg = "很遗憾，您中途放弃了"+industry_name+"领域，语言对 "+pair_name+" 的考试。您本月剩余考试次数为:"+myOverplusTimes
			+"。请多加练习，珍惜每一次考试机会哦。";
	    }else{
		msg = "很遗憾，您中途放弃了"+industry_name+"领域，语言对 "+pair_name+" 的考试。您本月考试次数已用尽，下个月再来参加考试吧。"
			+ "请多加练习，期待您下一个月的表现哦。";
	    }
	    sendExamTime(obj,examPaperBean.getTranslator_id(),msg);
	} catch (Exception e) {
	    Log4j.error("deleteExamPaper FAILED:" + e);
	    SendToPHP(obj, "FAILED");
	}
    }

    private void createExamPaper(ExamDAO examDAO, ExamPaperBean paper)
	    throws Exception {
	int examCount = WiitransConfig.getInstance(0).EXAM.EXAM_COUNT;
	int unknownJudgeMax = WiitransConfig.getInstance(0).EXAM.EXAM_MAX;
	int knownJudgeCount = (int) Math.round(examCount * 0.5);
	int unknownJudgeCount = (int) Math.round(examCount * 0.3);
	int subjectiveCount = examCount - knownJudgeCount - unknownJudgeCount;
	long s = System.currentTimeMillis();
	examDAO.insertExamPaper(paper);
	Log4j.debug("insertExamPaper -->" + (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	int test_id = paper.getTest_id();

	Map<String, Integer> param = new HashMap<String, Integer>();
	param.put("pair_id", paper.getPair_id());
	param.put("industry_id", paper.getIndustry_id());
	param.put("max", unknownJudgeMax);
	param.put("limit", unknownJudgeCount);
	List<StrategyBean> unKnowJudgeList = examDAO.getUnknownJudge(param);
	Log4j.debug("getUnknownJudge -->" + (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	// 不足 用已知选择题补
	if (unKnowJudgeList.size() < unknownJudgeCount) {
	    knownJudgeCount += (unknownJudgeCount - unKnowJudgeList.size());
	}
	param.put("limit", knownJudgeCount);
	List<JudgeBean> knownJudgeList = examDAO.getKnownJudge(param);
	Log4j.debug("getKnownJudge -->" + (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	param.put("limit", subjectiveCount);
	List<SubjectiveBean> subjectiveList = examDAO.getSubjective(param);
	Log4j.debug("getSubjective -->" + (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	// 添加试题
	int step = 0;
	// 已知
	for (JudgeBean judgeBean : knownJudgeList) {
	    ExamQuesBean ques = new ExamQuesBean();
	    ques.setTest_id(test_id);
	    ques.setQuestion_id(judgeBean.getJudge_id());
	    ques.setStep(step++);
	    ques.setScore(-1);// 未答题时为-1
	    ques.setType(Const.EXAM_QUES_TYPE_KNOWNJUDGE);
	    examDAO.insertExamQues(ques);
	}
	Log4j.debug("insertExamQues KNOWNJUDGE -->"
		+ (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	// 未知
	for (StrategyBean strategyBean : unKnowJudgeList) {
	    ExamQuesBean ques = new ExamQuesBean();
	    ques.setTest_id(test_id);
	    ques.setQuestion_id(strategyBean.getStrategy_id());
	    ques.setStep(step++);
	    ques.setScore(-1);// 未答题时为-1
	    ques.setType(Const.EXAM_QUES_TYPE_UNKNOWNJUDGE);
	    examDAO.insertExamQues(ques);
	}
	Log4j.debug("insertExamQues UNKNOWNJUDGE -->"
		+ (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	// 主观
	for (SubjectiveBean subjectiveBean : subjectiveList) {
	    ExamQuesBean ques = new ExamQuesBean();
	    ques.setTest_id(test_id);
	    ques.setQuestion_id(subjectiveBean.getSubjective_id());
	    ques.setStep(step++);
	    ques.setScore(-1);// 未答题时为-1
	    ques.setType(Const.EXAM_QUES_TYPE_SUBJECTIVE);
	    examDAO.insertExamQues(ques);
	}
	Log4j.debug("insertExamQues insertExamQues -->"
		+ (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
	examDAO.Commit();
	Log4j.debug("insertExamQues Commit -->"
		+ (System.currentTimeMillis() - s));
	s = System.currentTimeMillis();
    }

    private int SendToPHP(JSONObject obj, String result) {
	JSONObject resObj = new JSONObject();
	resObj.put("result", result);
	resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
	resObj.put(Const.BUNDLE_INFO_ID,
		Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
	resObj.put(Const.BUNDLE_INFO_ACTION_ID,
		Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

	return _reportor.Report(resObj);
    }
}
