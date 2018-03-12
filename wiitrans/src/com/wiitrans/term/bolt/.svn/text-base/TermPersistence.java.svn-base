package com.wiitrans.term.bolt;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import com.wiitrans.base.db.TermDAO;
import com.wiitrans.base.db.model.TermBean;
import com.wiitrans.base.db.model.TermDetailsBean;
import com.wiitrans.base.db.model.TermDetailsEvaBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.term.Term;
import com.wiitrans.base.term.TermMeta;
import com.wiitrans.base.term.UpdateTerm;

// 定期将更新的译员贡献术语同步到DB
public class TermPersistence extends Thread {

	// 新增译员贡献术语
	private LinkedBlockingQueue<Term> _newTerms = new LinkedBlockingQueue<Term>();
	// 译员评价贡献术语（点赞/反对）
	private LinkedBlockingQueue<UpdateTerm> _evaTerms = new LinkedBlockingQueue<UpdateTerm>();

	public TermPersistence() {
	}

	public int Start() {
		int ret = Const.FAIL;

		this.start();
		ret = Const.SUCCESS;

		return ret;
	}

	public int Stop() {
		int ret = Const.FAIL;

		return ret;
	}

	public void PushNewTerm(Term term) {
		try {
			_newTerms.put(term);
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	public void PushEvaTerm(UpdateTerm term) {
		try {
			_evaTerms.put(term);
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	public Term PopNewTerm() {
		Term term = null;

		try {
			term = _newTerms.poll();

		} catch (Exception e) {
			Log4j.error(e);
		}

		return term;
	}

	public UpdateTerm PopEvaTerm() {
		UpdateTerm term = null;

		try {
			term = _evaTerms.poll();

		} catch (Exception e) {
			Log4j.error(e);
		}

		return term;
	}

	public void run() {
		while (true) {
			TermDAO termdao = null;
			try {
				// 600s
				//sleep(600000);
				sleep(600000);

				// 取得队列中的新术语
				ArrayList<Term> newTermList = new ArrayList<Term>();
				Term newTerm = null;
				while (true) {
					newTerm = PopNewTerm();
					if (newTerm == null) {
						break;
					}
					newTermList.add(newTerm);
				}

				// 将队列中的评价
				ArrayList<UpdateTerm> updateTermList = new ArrayList<UpdateTerm>();
				UpdateTerm updateTerm = null;
				while (true) {
					updateTerm = PopEvaTerm();
					if (updateTerm == null) {
						break;
					}
					updateTermList.add(updateTerm);
				}

				// 将新术语和评价一起存入DB
				termdao = new TermDAO();
				termdao.Init(true);
				TermBean termbean;
				TermBean termbeantemp;
				TermDetailsBean termdetailsbean;
				TermDetailsBean termdetailsbeantemp;
				if (newTermList.size() > 0) {

					for (Term term : newTermList) {
						int term_id;
						if (term._pair_id > 0 && term._industryId > 0
								&& term._term != null
								&& term._term.length() > 0) {
							termbean = new TermBean();
							termbean.pair_id = term._pair_id;
							termbean.industry_id = term._industryId;
							termbean.term = term._term;
							termbeantemp = termdao.SelectTermByTerm(termbean);
							if (termbeantemp == null
									|| termbeantemp.term_id == 0) {
								termdao.InsertTerm(termbean);
								termdao.Commit();
								termbeantemp = termdao
										.SelectTermByTerm(termbean);
								term_id = termbeantemp.term_id;
							} else {
								term_id = termbeantemp.term_id;
							}

							if (term_id > 0) {
								if (term._meta != null && term._meta.size() > 0) {
									for (TermMeta termMeta : term._meta) {
										termdetailsbean = new TermDetailsBean();
										termdetailsbean.term_id = term_id;
										termdetailsbean.translator_id = termMeta._contributorUid;
										termdetailsbean.meaning = termMeta._meaning;
										termdetailsbean.usage = termMeta._usage;
										termdetailsbean.remark = termMeta._remark;

										termdetailsbeantemp = termdao
												.SelectTermDetails(termdetailsbean);
										if (termdetailsbeantemp == null
												|| termdetailsbeantemp.term_id == 0) {
											termdao.InsertTermDetails(termdetailsbean);
											termdao.Commit();
										}
									}

								} else {
									Log4j.log("贡献术语没有翻译用途备注等信息");
								}
							} else {
								Log4j.log("贡献术语数据库term_id(" + term_id + ")无效");
							}
						} else {
							Log4j.log("贡献术语所在语言对(" + term._pair_id
									+ ") industry(" + term._industryId
									+ ")无效或贡献术语为空");
						}
					}
				} else {
					Log4j.log("没有新贡献术语");
				}

				TermDetailsEvaBean termdetailsevabean;
				TermDetailsEvaBean termdetailsevabeantemp;
				if (updateTermList.size() > 0) {

					for (UpdateTerm updateterm : updateTermList) {
						if (updateterm._pairid > 0
								&& updateterm._industryId > 0
								&& updateterm._term != null
								&& updateterm._term.length() > 0
								&& updateterm._tid > 0 && updateterm._uid > 0) {
							termbean = new TermBean();
							termbean.pair_id = updateterm._pairid;
							termbean.industry_id = updateterm._industryId;
							termbean.term = updateterm._term;
							termbeantemp = termdao.SelectTermByTerm(termbean);
							if (termbeantemp != null
									&& termbeantemp.term_id > 0) {
								termdetailsbean = new TermDetailsBean();
								termdetailsbean.term_id = termbeantemp.term_id;
								termdetailsbean.translator_id = updateterm._uid;

								termdetailsbeantemp = termdao
										.SelectTermDetails(termdetailsbean);
								if (termdetailsbeantemp != null
										&& termdetailsbeantemp.term_id > 0) {

									termdetailsevabean = new TermDetailsEvaBean();
									termdetailsevabean.term_details_id = termdetailsbeantemp.term_details_id;
									termdetailsevabean.translator_id = updateterm._tid;
									termdetailsevabeantemp = termdao
											.SelectTermDetailsEva(termdetailsevabean);
									if (termdetailsevabeantemp == null) {
										if (updateterm.agree == 1) {
											termdetailsevabean.evaluation = 1;
											termdao.InsertTermDetailsEva(termdetailsevabean);
										} else if (updateterm.agree == -1) {
											termdetailsevabean.evaluation = 0;
											termdao.InsertTermDetailsEva(termdetailsevabean);
										}

									} else {
										if (updateterm.agree == 0) {
											termdao.DeleteTermDetailsEva(termdetailsevabeantemp.term_evaluation_id);
										} else if (updateterm.agree == 1) {
											termdetailsevabean.evaluation = 1;
											termdao.UpdateTermDetailsEva(termdetailsevabean);
										} else if (updateterm.agree == -1) {
											termdetailsevabean.evaluation = 0;
											termdao.UpdateTermDetailsEva(termdetailsevabean);
										}
									}
									termdao.Commit();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (termdao != null) {
					termdao.UnInit();
				}
			}
		}
	}
}
