package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface ExamPaperBeanMapper {
	public int insertExamPaper(ExamPaperBean paper);
	/**
	 * 加总试题分数并更新卷子分数
	 * @param paperId
	 * @return
	 */
	public int updateExamPaperScore(int paperId);
	/**
	 * 自动判卷时根据未知判断题更新相关卷子分数
	 * @param strategy_id
	 * @return
	 */
	public int updateAutoExamPaperScore(int strategy_id);
	/**
	 * 断是否判完所有卷子题，并更新状态（1.申请测试 2.审核 3.通过 4.未通过）
	 * @param strategy_id
	 * @return
	 */
	public int updateAutoExamPaperStatus(int strategy_id);
	public ExamPaperBean selectUnFinishedExamPaper(ExamPaperBean paper);
	public List<ExamPaperBean> selectAllUnFinishedExamPaper();
	public int selectExamMonthTimes(ExamPaperBean paper);
	public Map<String, String> selectExamMonthTimesMsg(ExamPaperBean paper);
	public void deleteUnFinishedExamPaper(int paperId);
}
