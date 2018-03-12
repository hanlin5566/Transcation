
package com.hzcf.hanson.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hzcf.workflow.action.Action;
import com.hzcf.workflow.context.Context;

/**
 * Create by hanlin on 2018年1月8日
 **/
public class LoggerConsolePriterAction extends ConsolePrinterAction{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public LoggerConsolePriterAction(Action action) {
		super();
	}
	@Override
	public Context handler(Context context) {
		logger.info("记录日志");
		return super.handler(context);
	}
	
	
	
}
