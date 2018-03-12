package com.hzcf.hanson.action;

import com.hzcf.workflow.action.Action;
import com.hzcf.workflow.context.Context;
import com.hzcf.workflow.node.Node;

/**
 * Create by hanlin on 2017年12月26日
 * 控制台输出动作
 **/
public class ConsolePrinterAction implements Action{
	public Context handler(Context context) {
		Node currentNode = context.getCurrentNode();
		System.out.println(currentNode.key()+"  "+currentNode.node().getText());
		return context;
	}
}
