package com.wiitrans.base.db;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.model.ExamPaperBean;
import com.wiitrans.base.db.model.ExamPaperBeanMapper;
import com.wiitrans.base.db.model.ExamQuesBean;
import com.wiitrans.base.db.model.ExamQuesBeanMapper;
import com.wiitrans.base.db.model.JudgeBean;
import com.wiitrans.base.db.model.JudgeBeanMapper;
import com.wiitrans.base.db.model.StrategyBean;
import com.wiitrans.base.db.model.StrategyBeanMapper;
import com.wiitrans.base.db.model.SubjectiveBean;
import com.wiitrans.base.db.model.SubjectiveBeanMapper;
import com.wiitrans.base.db.model.VoteBean;
import com.wiitrans.base.db.model.VoteBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ExamDAO extends CommonDAO {

	private JudgeBeanMapper _mapperjudge = null;
	private StrategyBeanMapper _mapperstrategy = null;
	private SubjectiveBeanMapper _mappersubjective = null;
	private ExamPaperBeanMapper _mapperexamPaper = null;
	private ExamQuesBeanMapper _mapperexamQues = null;
	private VoteBeanMapper _mappervote = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
			    _mapperjudge = _session.getMapper(JudgeBeanMapper.class);
			    _mapperstrategy = _session.getMapper(StrategyBeanMapper.class);
			    _mappersubjective = _session.getMapper(SubjectiveBeanMapper.class);
			    _mapperexamPaper = _session.getMapper(ExamPaperBeanMapper.class);
			    _mapperexamQues = _session.getMapper(ExamQuesBeanMapper.class);
			    _mappervote = _session.getMapper(VoteBeanMapper.class);
				if (_mapperjudge != null && _mapperstrategy != null && _mappersubjective != null
					&& _mapperexamPaper != null && _mapperexamQues != null && _mappervote != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<JudgeBean> getKnownJudge(Map<String,Integer> param) {
		return _mapperjudge.getKnownJudge(param);
	}
	
	public void movetoKnownJudge(Map<String,Integer> param){
	    _mapperjudge.movetoKnownJudge(param);
	}
	
	public List<StrategyBean> getUnknownJudge(Map<String,Integer> param) {
	    return _mapperstrategy.getUnknownJudge(param);
	}
	public void deleteUnknowJudge(int strategy_id) {
	    _mapperstrategy.deleteUnknowJudge(strategy_id);
	}
	public void deleteUnFinishedJudgeVote(int paperId) {
	    _mapperstrategy.deleteUnFinishedJudgeVote(paperId);
	}
	public void evalJudge(Map<String, Integer> param) {
	    _mapperstrategy.evalJudge(param);
	}
	public void updateAutoJudge(int strategy_id) {
	    _mapperstrategy.updateAutoJudge(strategy_id);
	}
	
	public List<SubjectiveBean> getSubjective(Map<String,Integer> param) {
	    return _mappersubjective.getSubjective(param);
	}
	public void evalSubjective(Map<String,Integer> param) {
	    _mappersubjective.evalSubjective(param);
	}
	public void deleteUnFinishedSubjectiveAnswer(int paperId) {
	    _mappersubjective.deleteUnFinishedSubjectiveAnswer(paperId);
	}
	public int insertExamPaper(ExamPaperBean paper) {
	    return _mapperexamPaper.insertExamPaper(paper);
	}
	public int updateExamPaperScore(int paperId) {
	    return _mapperexamPaper.updateExamPaperScore(paperId);
	}
	public int updateAutoExamPaperScore(int strategy_id) {
	    return _mapperexamPaper.updateAutoExamPaperScore(strategy_id);
	}
	public int updateAutoExamPaperStatus(int strategy_id) {
	    return _mapperexamPaper.updateAutoExamPaperStatus(strategy_id);
	}
	public int insertExamQues(ExamQuesBean ques) {
	    return _mapperexamQues.insertExamQues(ques);
	}
	public ExamQuesBean selectSubjectByStrategyId(int strategy_id) {
	    return _mapperexamQues.selectSubjectByStrategyId(strategy_id);
	}
	public void deleteUnFinishedExamQues(int paperId) {
	    _mapperexamQues.deleteUnFinishedExamQues(paperId);
	}
	public List<ExamQuesBean> selectStrategyByExamId(int paperId) {
	   return _mapperexamQues.selectStrategyByExamId(paperId);
	}
	
	public ExamPaperBean selectUnFinishedExamPaper(ExamPaperBean paper){
	    return _mapperexamPaper.selectUnFinishedExamPaper(paper);
	}
	public List<ExamPaperBean> selectAllUnFinishedExamPaper(){
	    return _mapperexamPaper.selectAllUnFinishedExamPaper();
	}
	public int selectExamMonthTimes(ExamPaperBean paper){
	    return _mapperexamPaper.selectExamMonthTimes(paper);
	}
	public Map<String, String> selectExamMonthTimesMsg(ExamPaperBean paper){
	    return _mapperexamPaper.selectExamMonthTimesMsg(paper);
	}
	public void deleteUnFinishedExamPaper(int paperId){
	    _mapperexamPaper.deleteUnFinishedExamPaper(paperId);
	}
	
	public int addVote(VoteBean voteBean){
	    return _mappervote.addVote(voteBean);
	}
	public void deleteVote(int strategy_id){
	    _mappervote.deleteVote(strategy_id);
	}
	
	public int getUnKnowJudgeVoteCount(int strategy_id){
	    return _mappervote.getUnKnowJudgeVoteCount(strategy_id);
	}
	public Map<String, BigDecimal> getJudgeVoteCount(int strategy_id){
	    return _mappervote.getJudgeVoteCount(strategy_id);
	}
	
}
