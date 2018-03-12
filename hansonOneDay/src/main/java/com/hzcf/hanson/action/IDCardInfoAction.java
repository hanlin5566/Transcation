package com.hzcf.hanson.action;

import com.hzcf.workflow.action.Action;
import com.hzcf.workflow.context.Context;

/**
 * Create by hanlin on 2018年1月15日
 **/
public class IDCardInfoAction extends ConsolePrinterAction implements Action{
	@Override
	public Context handler(Context context) {
		super.handler(context);
		context.getData().put("age", 16);
		return context;
	}
}
